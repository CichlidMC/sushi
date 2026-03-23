package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record ConstantExpressionSelector(ConstantDesc constant) implements ExpressionSelector {
	public static final MapCodec<ConstantExpressionSelector> CODEC = SushiCodecs.CONSTANT.xmap(
			ConstantExpressionSelector::new, ConstantExpressionSelector::constant
	).fieldOf("constant");

	@Override
	public Collection<Found> find(TransformableCode code) throws TransformException {
		List<Found> found = new ArrayList<>();

		for (InstructionHolder<?> instruction : code.instructions()) {
			if (instruction.get() instanceof ConstantInstruction constant && this.constant.equals(constant.constantValue())) {
				Selection selection = code.select().only(instruction);
				StackDelta delta = StackDelta.pushing(this.constantType());
				found.add(new Found(selection, delta));
			}
		}

		return found;
	}

	@Override
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC;
	}

	public ClassDesc constantType() {
		return switch (this.constant) {
			case ClassDesc clazz -> clazz;
			case MethodHandleDesc _ -> ConstantDescs.CD_MethodHandle;
			case MethodTypeDesc _ -> ConstantDescs.CD_MethodType;
			case Double _ -> ConstantDescs.CD_double;
			case Float _ -> ConstantDescs.CD_float;
			case Integer _ -> ConstantDescs.CD_int;
			case Long _ -> ConstantDescs.CD_long;
			case String _ -> ConstantDescs.CD_String;
			case DynamicConstantDesc<?> dynamic -> dynamic.constantType();
		};
	}

	/// Create a new selector based on the given [Constable]'s description.
	/// @throws IllegalArgumentException if the given constable cannot be described
	public static ConstantExpressionSelector of(Constable constable) throws IllegalArgumentException {
		Optional<? extends ConstantDesc> constant = constable.describeConstable();
		if (constant.isEmpty()) {
			throw new IllegalArgumentException("Value cannot be represented as a constant: " + constable);
		}

		return new ConstantExpressionSelector(constant.get());
	}
}
