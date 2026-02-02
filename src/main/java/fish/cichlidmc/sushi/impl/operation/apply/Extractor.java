package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.transformer.infra.OperationInfra;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.api.util.MethodGeneration;
import fish.cichlidmc.sushi.impl.operation.Extraction;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicCallSiteDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/// Manages an extraction as instructions are iterated.
///
/// The general idea is to capture all added instructions in a list, and then add them to a new lambda
/// method upon completion. There's a lot going on in here for the sake of managing local variables.
public final class Extractor {
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
	public void finish(Consumer<CodeBlock> output, ClassBuilder classBuilder) {
		StackDelta.MethodLike delta = this.extraction.delta();
		List<ClassDesc> params = delta.popped();
		ClassDesc returnType = delta.pushedOrVoid();

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
				OperationInfra.LMF, // standard LMF bootstrap
				OperationInfra.CALL_NAME, // interface method name
				lambda.factoryDesc, // params: captured variables; returnType: implemented interface
				// args - see LMF javadoc for info
				OperationInfra.CALL_DESC, lambda.handle, OperationInfra.CALL_DESC
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
		MethodGeneration.generate(
				classBuilder, lambda.handle.methodName(), lambda.handle.invocationType(),
				MethodGeneration.STATIC_LAMBDA_FLAGS, method -> method.withCode(code -> {
					// --- head: unpack Object[] parameters ---

					// invoke validation first, to make sure we have the right number of arguments
					code.aload(lambda.argsSlot); // push param array
					code.loadConstant(params.size()); // push expected size
					Instructions.invokeMethod(code, OperationInfra.CHECK_COUNT_HANDLE); // invoke validation, throws if it fails

					// unpack array
					for (int i = 0; i < params.size(); i++) {
						ClassDesc param = params.get(i);
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
								} else if (info.typeKind == TypeKind.REFERENCE) {
									// FIXME: if the local is expected to be anything other than Object, this will fail to verify
									// need a checkcast here, but need to know the right type
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
					if (returnType.equals(ConstantDescs.CD_void)) {
						// special case: return type of the lambda has to actually be Void
						code.aconst_null();
						code.areturn();
					} else {
						code.return_(TypeKind.from(returnType));
					}
				})
		);
	}

	private ExtractedLambda computeLambdaInfo() {
		ClassDesc returnType = this.extraction.delta().pushedOrBoxedVoid();
		List<LocalInfo> locals = new ArrayList<>(this.locals.values());
		locals.removeIf(info -> !info.crossesExtractionStart);
		return new ExtractedLambda(this.clazz, this.extraction.name(), returnType, locals);
	}

	private void updateLocalInfo(int slot, TypeKind typeKind, LocalInfo.Operation operation) {
		this.locals.computeIfAbsent(slot, s -> new LocalInfo(s, typeKind, operation)).update(typeKind, operation);
	}

	private LocalInfo getLocalInfo(int slot) {
		return Objects.requireNonNull(this.locals.get(slot), () -> "Unknown local in slot " + slot);
	}
}
