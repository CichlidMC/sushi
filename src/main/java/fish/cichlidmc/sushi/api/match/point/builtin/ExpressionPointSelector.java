package fish.cichlidmc.sushi.api.match.point.builtin;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// Injection point matching either right before or right after an arbitrary expression.
/// @param offset which end of the expression to target
public record ExpressionPointSelector(ExpressionSelector selector, Offset offset) implements PointSelector {
	public static final DualCodec<ExpressionPointSelector> CODEC = CompositeCodec.of(
			ExpressionSelector.CODEC.fieldOf("expression"), ExpressionPointSelector::selector,
			Offset.CODEC.optional(Offset.BEFORE).fieldOf("offset"), ExpressionPointSelector::offset,
			ExpressionPointSelector::new
	);

	public ExpressionPointSelector(ExpressionSelector selector) {
		this(selector, Offset.BEFORE);
	}

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return this.selector.find(code).stream().map(found -> switch (this.offset) {
					case BEFORE -> found.selection().start();
					case AFTER -> found.selection().end();
		}).toList();
	}

	@Override
	public MapCodec<? extends PointSelector> codec() {
		return CODEC.mapCodec();
	}
}
