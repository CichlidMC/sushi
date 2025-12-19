package fish.cichlidmc.sushi.api.target.inject.builtin;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeElement;
import java.lang.classfile.instruction.ReturnInstruction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Injection point targeting return instructions. May either match all returns, or one at a specific index.
public record ReturnInjectionPoint(int index) implements InjectionPoint {
	public static final ReturnInjectionPoint ALL = new ReturnInjectionPoint(-1);

	private static final Codec<Integer> indexCodec = Codec.INT.validate(
			index -> index > -1 ? Result.success(index) : Result.error("Index must be >= -1: " + index)
	);

	public static final MapCodec<ReturnInjectionPoint> CODEC = indexCodec.xmap(
			ReturnInjectionPoint::new, point -> point.index
	).fieldOf("index");

	public ReturnInjectionPoint {
		if (index < -1) {
			throw new IllegalArgumentException("Index must be >= -1: " + index);
		}
	}

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		List<Point> found = new ArrayList<>();

		int current = 0;

		for (CodeElement instruction : code.instructions().asList()) {
			if (instruction instanceof ReturnInstruction) {
				if (current == this.index || this.index == -1) {
					found.add(Point.before(instruction));
				}

				current++;
			}
		}

		return found;
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}
}
