package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;
import java.util.List;

public enum HeadInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<HeadInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<HeadInjectionPoint> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return List.of(Point.before(code.instructions().asList().getFirst()));
	}

	@Override
	public String describe() {
		return "head";
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
