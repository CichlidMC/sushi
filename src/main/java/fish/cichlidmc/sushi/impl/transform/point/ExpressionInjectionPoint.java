package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;

public record ExpressionInjectionPoint(ExpressionTarget target, boolean after) implements InjectionPoint {
	public static final MapCodec<ExpressionInjectionPoint> CODEC = CompositeCodec.of(
			ExpressionTarget.CODEC.fieldOf("target"), ExpressionInjectionPoint::target,
			Codec.BOOL.optional(false).fieldOf("after"), ExpressionInjectionPoint::after,
			ExpressionInjectionPoint::new
	);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		return this.target.find(code).stream()
				.map(found -> this.after ? found.selection().end() : found.selection().start())
				.toList();
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}
}
