package fish.cichlidmc.sushi.api.match.point.builtin;

import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;
import java.util.List;

/// Injection point targeting the head of the method, aka before the first instruction.
public enum HeadPointSelector implements PointSelector {
	INSTANCE;

	public static final Codec<HeadPointSelector> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<HeadPointSelector> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return List.of(Point.before(code.instructions().getFirst()));
	}

	@Override
	public MapCodec<? extends PointSelector> codec() {
		return MAP_CODEC;
	}
}
