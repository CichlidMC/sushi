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
import java.util.List;

public record ExpressionInjectionPoint(ExpressionTarget target, boolean after) implements InjectionPoint {
	public static final MapCodec<ExpressionInjectionPoint> CODEC = CompositeCodec.of(
			ExpressionTarget.CODEC.fieldOf("target"), ExpressionInjectionPoint::target,
			Codec.BOOL.optional(false).fieldOf("after"), ExpressionInjectionPoint::after,
			ExpressionInjectionPoint::new
	);

	@Override
	public Collection<Point> find(TransformableCode code) throws TransformException {
		ExpressionTarget.Found found = this.target.find(code);
		return found == null ? List.of() : found.selections().stream()
				.map(selection -> this.after ? selection.end() : selection.start())
				.toList();
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}
}
