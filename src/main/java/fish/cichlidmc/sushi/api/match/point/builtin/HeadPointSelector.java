package fish.cichlidmc.sushi.api.match.point.builtin;

import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
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

	/// A [PointTarget] for this selector.
	public static final PointTarget TARGET = new PointTarget(INSTANCE);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		// find the first real instruction
		for (InstructionHolder<?> instruction : code.instructions()) {
			if (instruction instanceof InstructionHolder.Real<?>) {
				return List.of(Point.before(instruction));
			}
		}

		return List.of();
	}

	@Override
	public MapCodec<? extends PointSelector> codec() {
		return MAP_CODEC;
	}
}
