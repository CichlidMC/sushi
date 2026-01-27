package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.transformer.infra.OperationInfra;
import fish.cichlidmc.sushi.impl.operation.Extraction;

import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// A lambda method that has been [extracted][Extraction].
public final class ExtractedLambda {
	public final DirectMethodHandleDesc handle;

	/// The list of locals which have been captured by this lambda
	public final List<LocalInfo> locals;

	/// The slot that the args array should be loaded from.
	public final int argsSlot;

	/// The [MethodTypeDesc] for the lambda factory.
	///
	/// Parameters are the captured locals, always returns an [Operation].
	public final MethodTypeDesc factoryDesc;

	/// Map of old local slots to new ones
	private final Map<Integer, Integer> localLookup;

	public ExtractedLambda(ClassModel owner, String name, ClassDesc returnType, List<LocalInfo> locals) {
		this.locals = locals;
		this.argsSlot = locals.size();

		ClassDesc[] localDescs = new ClassDesc[locals.size()];
		this.localLookup = new HashMap<>();

		for (int i = 0; i < locals.size(); i++) {
			LocalInfo local = locals.get(i);

			// we can't know the true type of the locals, since there might not even be one. bytecode is free
			// to reuse local slots as it sees fit. we're making an assumption that a given local slot will
			// only ever be used to hold values of a single TypeKind, which should hold for all typical bytecode.
			// if this assumption ever breaks, you're not allowed to submit a bug report unless it comes with a PR.
			// since we don't know the exact type, we'll go with the upper bound and cast as needed.
			localDescs[i] = local.parameterTypeKind().upperBound();

			// just use the index as the new slot, we're safe to start at 0 since it's a fresh method
			// and captured arguments come before lambda method arguments (ex. IntRef, double, Object[])
			this.localLookup.put(local.slot, i);
		}

		this.factoryDesc = MethodTypeDesc.of(OperationInfra.OPERATION_DESC, localDescs);

		// the arguments of the backing method will be the locals + an Object[], add a slot for it
		ClassDesc[] arguments = Arrays.copyOf(localDescs, localDescs.length + 1);
		arguments[localDescs.length] = ConstantDescs.CD_Object.arrayType();
		MethodTypeDesc desc = MethodTypeDesc.of(returnType, arguments);

		boolean isInterface = owner.flags().flags().contains(AccessFlag.INTERFACE);
		DirectMethodHandleDesc.Kind invokeKind = isInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
		this.handle = MethodHandleDesc.ofMethod(invokeKind, owner.thisClass().asSymbol(), name, desc);
	}

	public int remapLocal(TypeKind typeKind, int oldSlot, CodeBuilder builder) {
		Integer newSlot = this.localLookup.get(oldSlot);
		if (newSlot != null)
			return newSlot;

		// for locals that are defined within the extraction, we won't have a value initially.
		// allocate a new slot and save it for later.
		int slot = builder.allocateLocal(typeKind);
		this.localLookup.put(oldSlot, slot);
		return slot;
	}
}
