package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.field.FieldTarget;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/// Matches field gets and sets, both static and non-static.
public final class FieldExpressionSelector implements ExpressionSelector {
	public static final DualCodec<FieldExpressionSelector> CODEC = CompositeCodec.of(
			FieldTarget.CODEC.fieldOf("field"), selector -> selector.target,
			ClassDescs.CLASS_CODEC.optional().fieldOf("owner"), selector -> selector.owner,
			Operation.CODEC.fieldOf("operation"), selector -> selector.operation,
			Codec.BOOL.fieldOf("static"), selector -> selector.isStatic,
			FieldExpressionSelector::new
	);

	public final FieldTarget target;
	public final Optional<ClassDesc> owner;
	public final Operation operation;
	public final boolean isStatic;

	private final Opcode opcode;

	public FieldExpressionSelector(FieldTarget target, Optional<ClassDesc> owner, Operation operation, boolean isStatic) {
		this.target = target;
		this.owner = owner;
		this.operation = operation;
		this.isStatic = isStatic;
		this.opcode = operation.opcode(isStatic);
	}

	@Override
	public Collection<Found> find(TransformableCode code) throws TransformException {
		List<Found> found = new ArrayList<>();

		for (InstructionHolder<?> instruction : code.instructions()) {
			if (!(instruction.get() instanceof FieldInstruction fieldInstruction))
				continue;

			if (!this.target.selector().matches(fieldInstruction))
				continue;

			ClassDesc owner = fieldInstruction.owner().asSymbol();
			if (this.owner.isPresent() && !this.owner.get().equals(owner))
				continue;

			if (this.opcode != fieldInstruction.opcode())
				continue;

			ClassDesc type = fieldInstruction.typeSymbol();
			StackDelta delta = this.operation.delta(owner, type, this.isStatic);
			found.add(new Found(code.select().only(instruction), delta));
		}

		return found;
	}

	@Override
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC.mapCodec();
	}

	public static FieldExpressionSelector get(FieldTarget target, Optional<ClassDesc> owner, boolean isStatic) {
		return new FieldExpressionSelector(target, owner, Operation.GET, isStatic);
	}

	public static FieldExpressionSelector set(FieldTarget target, Optional<ClassDesc> owner, boolean isStatic) {
		return new FieldExpressionSelector(target, owner, Operation.SET, isStatic);
	}

	public enum Operation {
		GET(Opcode.GETFIELD, Opcode.GETSTATIC),
		SET(Opcode.PUTFIELD, Opcode.PUTSTATIC);

		public static final Codec<Operation> CODEC = Codec.byName(Operation.class, op -> op.name().toLowerCase(Locale.ROOT));

		private final Opcode instanceOpcode;
		private final Opcode staticOpcode;

		Operation(Opcode instanceOpcode, Opcode staticOpcode) {
			this.instanceOpcode = instanceOpcode;
			this.staticOpcode = staticOpcode;
		}

		public Opcode opcode(boolean isStatic) {
			return isStatic ? this.staticOpcode : this.instanceOpcode;
		}

		public StackDelta delta(ClassDesc owner, ClassDesc type, boolean isStatic) {
			return isStatic ? switch (this) {
				case GET -> StackDelta.of(List.of(), type);
				case SET -> StackDelta.popOnly(List.of(type));
			} : switch (this) {
				case GET -> StackDelta.of(List.of(owner), List.of(type));
				case SET -> StackDelta.popOnly(List.of(owner, type));
			};
		}
	}
}
