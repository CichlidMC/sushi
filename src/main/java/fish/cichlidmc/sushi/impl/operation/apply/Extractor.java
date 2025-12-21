package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/// Manages an extraction as instructions are iterated.
///
/// The general idea is to capture all added instructions in a list, and then add them to a new lambda
/// method upon completion. There's a lot going on in here for the sake of managing local variables.
public final class Extractor {
	public static final DirectMethodHandleDesc CHECK_COUNT_HANDLE = MethodHandleDesc.ofMethod(
			DirectMethodHandleDesc.Kind.STATIC,
			ClassDescs.of(ExtractionValidation.class),
			"checkCount",
			MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_int)
	);

	/// @see LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)
	public static final DirectMethodHandleDesc LMF = ConstantDescs.ofCallsiteBootstrap(
			ClassDescs.of(LambdaMetafactory.class), "metafactory", ConstantDescs.CD_CallSite,
			// args
			ConstantDescs.CD_MethodType, ConstantDescs.CD_MethodHandle, ConstantDescs.CD_MethodType
	);

	public static final MethodTypeDesc OPERATION_CALL_DESC = MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object.arrayType());

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
			this.updateLocalInfo(load.slot(), load.typeKind(), LocalInfo.Operation.LOAD);
		} else if (element instanceof StoreInstruction store) {
			this.updateLocalInfo(store.slot(), store.typeKind(), LocalInfo.Operation.STORE);
		}
	}

	/// Called when this extraction is complete, finalizing it
	public void finish(Consumer<CodeBlock> output, MethodGenerator methodGenerator) {
		MethodTypeDesc extractionDesc = this.extraction.desc();
		ClassDesc[] params = extractionDesc.parameterArray();
		ClassDesc returnType = extractionDesc.returnType();

		ExtractedLambda lambda = this.computeLambdaInfo();

		// if any locals are stored or loaded within this extraction, we need to make them accessible to the extracted lambda.
		// if a local is only read, we can simply pass it along.
		// if a local is written, then we need to wrap it in a Ref so the original can be updated.
		// the necessary locals will be provided as captured lambda arguments, which get consumed when the Operation is created.

		// first, we go though each local we've seen and push it to the stack.
		// if the local needs to be mutable, we wrap it in the proper Ref class.
		// refSlots tracks the newly allocated slots used by the created Refs.
		Map<LocalInfo, Integer> refSlots = new HashMap<>();
		output.accept(builder -> lambda.locals.forEach(local -> {
			if (local.isMutable()) {
				local.refType.constructParameterized(builder, local::load);
				int refSlot = builder.allocateLocal(TypeKind.REFERENCE);
				// load/store produces nicer bytecode, matches javac
				builder.storeLocal(TypeKind.REFERENCE, refSlot);
				builder.loadLocal(TypeKind.REFERENCE, refSlot);
				refSlots.put(local, refSlot);
			} else {
				local.load(builder);
			}
		}));

		// create and push the Operation.
		output.accept(builder -> builder.invokedynamic(DynamicCallSiteDesc.of(
				LMF, // standard LMF bootstrap
				"call", // interface method name
				lambda.factoryDesc, // params: captured variables; returnType: implemented interface
				// args - see LMF javadoc for info
				OPERATION_CALL_DESC, lambda.handle, OPERATION_CALL_DESC
		)));

		// write the extraction's block
		output.accept(this.extraction.block());

		// after execution, we need to go through each mutable local's Ref and update the values of the original variables.
		output.accept(builder -> refSlots.forEach((local, refSlot) -> {
			// load ref and get value
			builder.loadLocal(TypeKind.REFERENCE, refSlot);
			local.refType.invokeGet(builder);
			// update local
			builder.storeLocal(local.typeKind, local.slot);
			// load again and discard
			builder.loadLocal(TypeKind.REFERENCE, refSlot);
			local.refType.invokeDiscard(builder);
		}));

		// generate the lambda method
		methodGenerator.generate(lambda.handle.methodName(), lambda.handle.invocationType(), LAMBDA_FLAGS, method -> method.withCode(code -> {
			// --- head: unpack Object[] parameters ---

			// invoke validation first, to make sure we have the right number of arguments
			code.aload(lambda.argsSlot); // push param array
			code.loadConstant(params.length); // push expected size
			Instructions.invokeMethod(code, CHECK_COUNT_HANDLE); // invoke validation, throws if it fails

			// unpack array
			for (int i = 0; i < params.length; i++) {
				ClassDesc param = params[i];
				code.aload(lambda.argsSlot); // push array
				code.loadConstant(i); // push index
				code.aaload(); // read from array - always a reference, it's an Object[]

				if (param.isPrimitive()) {
					// must unbox boxed primitives that were boxed to be stored in the Object[]
					Instructions.unboxChecked(code, param);
				} else {
					// validate type for non-primitives
					code.checkcast(param);
				}
			}

			// --- body: add all elements ---
			for (CodeElement element : this.elements) {
				// we need to remap the slots referenced by loads and stores.
				switch (element) {
					case LoadInstruction load -> {
						LocalInfo info = this.getLocalInfo(load.slot());
						if (!info.crossesExtractionStart) {
							// self-contained, leave it alone
							code.with(load);
							break;
						}

						int newSlot = lambda.remapLocal(load.typeKind(), load.slot(), code);
						code.loadLocal(info.parameterTypeKind(), newSlot);

						if (info.isMutable()) {
							code.checkcast(info.refType.impl);
							info.refType.invokeGet(code);
						}
					}
					case StoreInstruction store -> {
						LocalInfo info = this.getLocalInfo(store.slot());
						if (!info.crossesExtractionStart) {
							// self-contained, leave it alone
							code.with(store);
							break;
						}

						int newSlot = lambda.remapLocal(store.typeKind(), store.slot(), code);
						if (info.isMutable()) {
							code.loadLocal(TypeKind.REFERENCE, newSlot);
							code.checkcast(info.refType.impl);
							info.refType.invokeSetStatic(code);
						} else {
							code.storeLocal(store.typeKind(), newSlot);
						}
					}
					// everything else just gets passed on as-is
					default -> code.with(element);
				}
			}

			// --- tail: return ---
			code.return_(TypeKind.from(returnType));
		}));
	}

	private ExtractedLambda computeLambdaInfo() {
		MethodTypeDesc extractionDesc = this.extraction.desc();
		List<LocalInfo> locals = new ArrayList<>(this.locals.values());
		locals.removeIf(info -> !info.crossesExtractionStart);
		return new ExtractedLambda(this.clazz, this.extraction.name(), extractionDesc.returnType(), locals);
	}

	private void updateLocalInfo(int slot, TypeKind typeKind, LocalInfo.Operation operation) {
		this.locals.computeIfAbsent(slot, s -> new LocalInfo(s, typeKind, operation)).update(typeKind, operation);
	}

	private LocalInfo getLocalInfo(int slot) {
		return Objects.requireNonNull(this.locals.get(slot), () -> "Unknown local in slot " + slot);
	}
}
