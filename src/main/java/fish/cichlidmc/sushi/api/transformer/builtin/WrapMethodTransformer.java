package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.OperationInfra;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.api.util.MethodGeneration;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.PseudoInstruction;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

public final class WrapMethodTransformer extends HookingTransformer {
	public static final DualCodec<WrapMethodTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), SimpleTransformer::classPredicate,
			MethodTarget.CODEC.fieldOf("method"), transformer -> transformer.method,
			Hook.PARAMETERLESS_CODEC.codec().fieldOf("hook"), transformer -> transformer.hook,
			WrapMethodTransformer::new
	);

	public WrapMethodTransformer(ClassPredicate predicate, MethodTarget method, Hook hook) {
		if (!hook.params().isEmpty()) {
			throw new IllegalArgumentException("WrapMethod forbids context parameters on hooks");
		}

		// allowing slices here would cause the total annihilation of the trout population
		super(predicate, method, Slice.NONE, hook);
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		String methodName = code.owner().key().name();
		if (methodName.equals("<init>")) {
			throw new TransformException("Constructors cannot be wrapped");
		} else if (methodName.equals("<clinit>")) {
			throw new TransformException("Static init cannot be wrapped");
		}

		code.transformDirect(ctx -> new Wrapper(ctx, provider));
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}

	private static final class Wrapper implements CodeTransform {
		private final DirectTransform.Context.Code context;
		private final HookProvider hookProvider;
		private final List<CodeElement> instructions;

		private Wrapper(DirectTransform.Context.Code context, HookProvider hookProvider) {
			this.context = context;
			this.hookProvider = hookProvider;
			this.instructions = new ArrayList<>();
		}

		@Override
		public void accept(CodeBuilder builder, CodeElement element) {
			switch (Mode.of(element)) {
				case WRAP -> this.instructions.add(element);
				case KEEP -> builder.with(element);
				case DROP -> {}
			}
		}

		@Override
		public void atEnd(CodeBuilder builder) {
			Id owner = this.context.transformContext().transformerId();
			TransformableClass clazz = this.context.transformContext().target();
			TransformableMethod method = this.context.code().owner();

			// create lambda to hold wrapped code
			String lambdaName = clazz.createUniqueMethodName("wrap_method", owner);
			MethodTypeDesc desc = normalizeMethodDesc(method);

			// we need to convert void methods into Void lambdas
			boolean isVoid = desc.returnType().equals(ConstantDescs.CD_void);
			ClassDesc lambdaReturnType = isVoid ? ConstantDescs.CD_Void : desc.returnType();
			MethodTypeDesc lambdaDesc = MethodTypeDesc.of(lambdaReturnType, ConstantDescs.CD_Object.arrayType());

			MethodGeneration.generate(
					this.context.classBuilder(), lambdaName, lambdaDesc,
					MethodGeneration.STATIC_LAMBDA_FLAGS,
					methodBuilder -> methodBuilder.withCode(code -> {
						// check argument array size
						code.aload(0); // push array
						code.loadConstant(desc.parameterCount()); // push expected size
						Instructions.invokeMethod(code, OperationInfra.CHECK_COUNT_HANDLE);

						// unpack args into local slots
						for (int i = 0; i < desc.parameterCount(); i++) {
							ClassDesc param = desc.parameterType(i);
							code.aload(0); // push array
							code.loadConstant(i); // push index
							code.aaload(); // read from array - always a reference, it's an Object[]

							if (param.isPrimitive()) {
								// must unbox boxed primitives that were boxed to be stored in the Object[]
								Instructions.unboxChecked(code, param);
							} else {
								// validate type for non-primitives
								code.checkcast(param);
							}

							// store it. add 1, since 0 holds the arg array.
							code.storeLocal(TypeKind.from(param), i + 1);
						}

						// add all wrapped instructions.
						this.instructions.forEach(instruction -> code.with(switch (instruction) {
							// loads and stores need their slots bumped by one to make room for the arg array.
							case LoadInstruction load -> LoadInstruction.of(load.typeKind(), load.slot() + 1);
							case StoreInstruction store -> StoreInstruction.of(store.typeKind(), store.slot() + 1);
							// when the return type is void, we also need to convert each return to an ICONST_NULL + ARETURN.
							case ReturnInstruction _ when isVoid -> {
								code.aconst_null();
								yield ReturnInstruction.of(Opcode.ARETURN);
							}
							default -> instruction;
						}));
					})
			);

			// write code to invoke lambda.
			// first, push all arguments for the hook to consume.
			for (int i = 0; i < desc.parameterCount(); i++) {
				ClassDesc param = desc.parameterType(i);
				builder.loadLocal(TypeKind.from(param), i);
			}

			// next, generate and push the Operation
			DirectMethodHandleDesc lambdaHandle = MethodHandleDesc.ofMethod(
					DirectMethodHandleDesc.Kind.STATIC,
					clazz.desc(), lambdaName, lambdaDesc
			);

			builder.invokedynamic(DynamicCallSiteDesc.of(
					OperationInfra.LMF,
					OperationInfra.CALL_NAME,
					OperationInfra.DEFAULT_INVOCATION_TYPE,
					// args - see LMF javadoc for info
					OperationInfra.CALL_DESC, lambdaHandle, OperationInfra.CALL_DESC
			));

			// finally, invoke the hook
			List<ClassDesc> hookParams = new ArrayList<>(desc.parameterList());
			hookParams.add(OperationInfra.OPERATION_DESC);

			DirectMethodHandleDesc hook = this.hookProvider.get(desc.returnType(), hookParams);
			Instructions.invokeMethod(builder, hook);
			if (!hook.invocationType().returnType().equals(desc.returnType())) {
				// coerced to a weaker type, checkcast
				builder.checkcast(desc.returnType());
			}

			// and return
			builder.return_(TypeKind.from(desc.returnType()));
		}

		private static MethodTypeDesc normalizeMethodDesc(TransformableMethod method) {
			MethodTypeDesc desc = method.key().desc();
			if (method.model().flags().flags().contains(AccessFlag.STATIC))
				return desc;

			return desc.insertParameterTypes(0, method.owner().desc());
		}

		private enum Mode {
			WRAP, KEEP, DROP;

			private static Mode of(CodeElement element) {
				if (element instanceof Instruction)
					return WRAP;

				if (element instanceof PseudoInstruction) {
					return element instanceof LocalVariable ? DROP : WRAP;
				}

				return KEEP;
			}
		}
	}
}
