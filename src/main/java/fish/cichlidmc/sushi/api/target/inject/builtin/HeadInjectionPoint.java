package fish.cichlidmc.sushi.api.target.inject.builtin;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;
import java.util.List;

/**
 * Injection point targeting the head of the method, aka before the first instruction.
 */
public enum HeadInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<HeadInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<HeadInjectionPoint> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return List.of(Point.before(code.instructions().asList().getFirst()));
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
