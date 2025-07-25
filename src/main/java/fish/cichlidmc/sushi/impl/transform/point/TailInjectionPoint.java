package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.util.Collection;
import java.util.List;

public enum TailInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<TailInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<TailInjectionPoint> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		// walk backwards from the end until a return is found
		for (CodeElement instruction : code.instructions().asList().reversed()) {
			if (instruction instanceof ReturnInstruction) {
				return List.of(Point.before(instruction));
			}
		}

		return List.of();
	}

	@Override
	public String describe() {
		return "tail";
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
