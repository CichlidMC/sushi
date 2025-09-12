package fish.cichlidmc.sushi.api.target.inject.builtin;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Injection point targeting return instructions. May either match all returns, or one at a specific index.
 */
public class ReturnInjectionPoint implements InjectionPoint {
	public static final ReturnInjectionPoint ALL = new ReturnInjectionPoint(-1);

	public static final MapCodec<ReturnInjectionPoint> CODEC = Codec.INT.xmap(
			ReturnInjectionPoint::new, point -> point.index
	).fieldOf("index");

	private final int index;

	public ReturnInjectionPoint(int index) {
		this.index = index;
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
