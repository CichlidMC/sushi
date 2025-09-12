package fish.cichlidmc.sushi.api.target.inject.builtin;

import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;

/**
 * Injection point matching either right before or right after an arbitrary expression.
 * @param offset which end of the expression to target
 */
public record ExpressionInjectionPoint(ExpressionTarget target, Offset offset) implements InjectionPoint {
	public static final MapCodec<ExpressionInjectionPoint> CODEC = CompositeCodec.of(
			ExpressionTarget.CODEC.fieldOf("target"), ExpressionInjectionPoint::target,
			Offset.CODEC.optional(Offset.BEFORE).fieldOf("offset"), ExpressionInjectionPoint::offset,
			ExpressionInjectionPoint::new
	);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return this.target.find(code).stream().map(found -> switch (this.offset) {
					case BEFORE -> found.selection().start();
					case AFTER -> found.selection().end();
		}).toList();
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}
}
