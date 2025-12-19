package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.impl.operation.Extraction;
import fish.cichlidmc.sushi.impl.operation.runtime.ExtractionValidation;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;

import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/// Manages an extraction as instructions are iterated.
public final class Extractor {
	public static final ClassDesc VALIDATION_DESC = ClassDescs.of(ExtractionValidation.class);
	public static final String CHECK_COUNT = "checkCount";
	public static final MethodTypeDesc CHECK_COUNT_DESC = MethodTypeDesc.of(
			ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_int
	);

	/// @see LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)
	public static final DirectMethodHandleDesc LMF = ConstantDescs.ofCallsiteBootstrap(
			ClassDescs.of(LambdaMetafactory.class), "metafactory", ConstantDescs.CD_CallSite,
			// args
			ConstantDescs.CD_MethodType, ConstantDescs.CD_MethodHandle, ConstantDescs.CD_MethodType
	);

	public static final MethodTypeDesc OPERATION_TYPE = MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object.arrayType());

	// no captured args, just returns an Operation
	public static final MethodTypeDesc OPERATION_LAMBDA_FACTORY = MethodTypeDesc.of(ClassDescs.of(Operation.class));

	public static final Set<AccessFlag> LAMBDA_FLAGS = EnumSet.copyOf(Set.of(AccessFlag.PRIVATE, AccessFlag.STATIC, AccessFlag.SYNTHETIC));

	public final Extraction extraction;

	private final ClassModel clazz;
	private final List<CodeElement> elements;
	private final Map<Integer, LocalInfo> locals;

	public Extractor(ClassModel clazz, Extraction extraction) {
		this.clazz = clazz;
		this.extraction = extraction;
		this.elements = new ArrayList<>();
		this.locals = new HashMap<>();
	}

	public void intercept(CodeElement element) {
		this.elements.add(element);

		// extracted code is allowed to reference locals.
		// need to find each index, check if it was read, written, or both, and then generate the proper infrastructure
		if (element instanceof LoadInstruction load) {
			this.locals.computeIfAbsent(load.slot(), LocalInfo::new).read = true;
		} else if (element instanceof StoreInstruction store) {
			this.locals.computeIfAbsent(store.slot(), LocalInfo::new).written = true;
		}
	}

	public void finish(Consumer<CodeBlock> output, MethodGenerator methodGenerator) {
		MethodTypeDesc extractionDesc = this.extraction.desc();
		MethodTypeDesc lambdaDesc = MethodTypeDesc.of(extractionDesc.returnType(), ConstantDescs.CD_Object.arrayType());
		ClassDesc[] params = extractionDesc.parameterArray();
		ClassDesc returnType = extractionDesc.returnType();

		// generate the lambda method
		methodGenerator.generate(this.extraction.name(), lambdaDesc, LAMBDA_FLAGS, method -> method.withCode(code -> {
			// head: unpack Object[] parameters

			// invoke validation
			code.aload(0); // load param array
			code.ldc(params.length);
			code.invokestatic(VALIDATION_DESC, CHECK_COUNT, CHECK_COUNT_DESC);

			// unpack array
			for (int i = 0; i < params.length; i++) {
				ClassDesc param = params[i];
				code.aload(0); // push array
				code.loadConstant(i); // push index
				code.aaload(); // read from array - always a reference, it's an Object[]

				if (param.isPrimitive()) {
					// must convert boxed primitives back to unboxed
					// check the boxed type first
					Instructions.maybeUnbox(code, param);
				} else {
					// validate type for non-primitives
					code.checkcast(param);
				}
			}

			// body: add all elements
			for (CodeElement element : this.elements) {
				// TODO: fix locals
				code.with(element);
			}

			// tail: return
			code.return_(TypeKind.from(returnType));
		}));


		CodeBlock invoker = this.makeInvoker(lambdaDesc);
		output.accept(builder -> this.extraction.block().write(builder, invoker));
	}

	private CodeBlock makeInvoker(MethodTypeDesc lambdaDesc) {
		boolean targetIsInterface = this.clazz.flags().flags().contains(AccessFlag.INTERFACE);
		DirectMethodHandleDesc.Kind kind = targetIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
		DirectMethodHandleDesc lambdaHandle = MethodHandleDesc.ofMethod(kind, this.clazz.thisClass().asSymbol(), this.extraction.name(), lambdaDesc);

		return builder -> {
			// push the Operation to the stack
			builder.invokedynamic(DynamicCallSiteDesc.of(
					LMF, // bootstrap
					"call", // interface method name
					OPERATION_LAMBDA_FACTORY, // lambda factory
					// args - see LMF javadoc for info
					OPERATION_TYPE, lambdaHandle, OPERATION_TYPE
			));
		};
	}

	private static final class LocalInfo {
		private boolean read;
		private boolean written;

		private LocalInfo(int ignoredSlot) {
		}
	}
}
