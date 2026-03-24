package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.match.SlicedSelector;
import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// A [SlicedSelector] targeting [expressions][ExpressionSelector].
/// @see SlicedSelector
public final class SlicedExpressionSelector extends SlicedSelector<ExpressionSelector> implements ExpressionSelector {
	public static final DualCodec<SlicedExpressionSelector> CODEC = codec(ExpressionSelector.CODEC, SlicedExpressionSelector::new);

	public SlicedExpressionSelector(PointSelector from, PointSelector to, ExpressionSelector selector) {
		super(from, to, selector);
	}

	@Override
	public Collection<Found> find(TransformableCode code) throws TransformException {
		return this.selector.find(this.slice(code));
	}

	@Override
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC.mapCodec();
	}

	public static SlicedExpressionSelector from(PointSelector from, ExpressionSelector selector) {
		return new SlicedExpressionSelector(from, TailPointSelector.INSTANCE, selector);
	}

	public static SlicedExpressionSelector to(PointSelector to, ExpressionSelector selector) {
		return new SlicedExpressionSelector(HeadPointSelector.INSTANCE, to, selector);
	}
}
