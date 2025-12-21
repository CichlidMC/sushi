package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.ref.Refs;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;

/// Tracks information about a local variable slot in a method.
/// Used to fix locals caught in extractions.
public final class LocalInfo {
	public final int slot;

	/// The [TypeKind] of values stored in this local slot.
	/// We assume that each slot will only hold variables of one [TypeKind].
	/// This assumption is incorrect, but should hold true in any reasonable bytecode.
	public final TypeKind typeKind;

	/// The type of reference to use when wrapping this local.
	public final Refs.Type refType;

	/// True if this local crosses over the beginning of the extraction.
	///
	/// This is based on the logic that if the first operation applied to a local is a LOAD, then
	/// there must be a previous value that was stored before the extraction began.
	///
	/// It's known that this will cause Issues if a local is defined in the extraction, but then
	/// continues to be referenced afterward. Fixing that would be hard, and shouldn't really happen anyway.
	public final boolean crossesExtractionStart;

	private boolean mutable;

	public LocalInfo(int slot, TypeKind typeKind, Operation firstOperation) {
		this.slot = slot;
		this.typeKind = typeKind;
		this.refType = Refs.Type.of(typeKind);
		this.crossesExtractionStart = firstOperation == Operation.LOAD;
	}

	/// @return true if this local is ever [stored][Operation#STORE] during the scope of the extraction
	public boolean isMutable() {
		return this.mutable;
	}

	/// Update this local slot with information about a load or store.
	public void update(TypeKind typeKind, Operation operation) {
		if (typeKind != this.typeKind) {
			throw new IllegalStateException("TypeKind stored in slot " + this.slot + " has changed");
		}

		if (operation == Operation.STORE) {
			this.mutable = true;
		}
	}

	/// Load the local in this slot.
	public void load(CodeBuilder builder) {
		builder.loadLocal(this.typeKind, this.slot);
	}

	/// @return the [TypeKind] of this local when passing it as a method parameter
	public TypeKind parameterTypeKind() {
		return this.mutable ? TypeKind.REFERENCE : this.typeKind;
	}

	/// The operations that can be applied to local variables.
	public enum Operation {
		LOAD, STORE
	}
}
