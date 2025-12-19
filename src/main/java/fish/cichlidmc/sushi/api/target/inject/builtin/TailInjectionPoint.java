package fish.cichlidmc.sushi.api.target.inject.builtin;

import fish.cichlidmc.sushi.api.model.code.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.instruction.ReturnInstruction;
import java.util.Collection;
import java.util.List;

/// Injection point targeting the tail of the method, aka before the last instruction.
public enum TailInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<TailInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<TailInjectionPoint> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		// walk backwards from the end until a return is found
		for (InstructionHolder<?> instruction : code.instructions().reversed()) {
			if (instruction.get() instanceof ReturnInstruction) {
				return List.of(Point.before(instruction));
			}
		}

		return List.of();
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
