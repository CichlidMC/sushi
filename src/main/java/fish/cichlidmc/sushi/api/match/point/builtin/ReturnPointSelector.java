package fish.cichlidmc.sushi.api.match.point.builtin;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.instruction.ReturnInstruction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Injection point targeting return instructions. May either match all returns, or one at a specific index.
public record ReturnPointSelector(int index) implements PointSelector {
	/// Selector that selects all return instructions.
	public static final ReturnPointSelector ALL = new ReturnPointSelector(-1);
	/// A [PointTarget] for [#ALL], with unlimited matches.
	public static final PointTarget ALL_TARGET = new PointTarget(ALL, Target.UNLIMITED);

	private static final Codec<Integer> indexCodec = Codec.INT.validate(
			index -> index > -1 ? Result.success(index) : Result.error("Index must be >= -1: " + index)
	);

	public static final MapCodec<ReturnPointSelector> CODEC = indexCodec.xmap(
			ReturnPointSelector::new, point -> point.index
	).fieldOf("index");

	public ReturnPointSelector {
		if (index < -1) {
			throw new IllegalArgumentException("Index must be >= -1: " + index);
		}
	}

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		List<Point> found = new ArrayList<>();

		int current = 0;

		for (InstructionHolder<?> instruction : code.instructions()) {
			if (instruction.get() instanceof ReturnInstruction) {
				if (current == this.index || this.index == -1) {
					found.add(Point.before(instruction));
				}

				current++;
			}
		}

		return found;
	}

	@Override
	public MapCodec<? extends PointSelector> codec() {
		return CODEC;
	}
}
