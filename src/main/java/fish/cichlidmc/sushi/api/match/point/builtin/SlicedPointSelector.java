package fish.cichlidmc.sushi.api.match.point.builtin;

import fish.cichlidmc.sushi.api.match.SlicedSelector;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// A [SlicedSelector] targeting [points][PointSelector].
/// @see SlicedSelector
public final class SlicedPointSelector extends SlicedSelector<PointSelector> implements PointSelector {
	public static final DualCodec<SlicedPointSelector> CODEC = codec(PointSelector.CODEC, SlicedPointSelector::new);

	public SlicedPointSelector(PointSelector from, PointSelector to, PointSelector selector) {
		super(from, to, selector);
	}

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return this.selector.find(this.slice(code));
	}

	@Override
	public MapCodec<? extends PointSelector> codec() {
		return CODEC.mapCodec();
	}

	public static SlicedPointSelector from(PointSelector from, PointSelector selector) {
		return new SlicedPointSelector(from, TailPointSelector.INSTANCE, selector);
	}

	public static SlicedPointSelector to(PointSelector to, PointSelector selector) {
		return new SlicedPointSelector(HeadPointSelector.INSTANCE, to, selector);
	}
}
