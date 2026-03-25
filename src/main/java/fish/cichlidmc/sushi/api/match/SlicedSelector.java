package fish.cichlidmc.sushi.api.match;

import fish.cichlidmc.sushi.api.match.expression.builtin.SlicedExpressionSelector;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.SlicedPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.transformer.slice.SlicedTransformableCode;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.util.Collection;
import java.util.NavigableSet;

/// Base class for [SlicedPointSelector] and [SlicedExpressionSelector].
///
/// A sliced selector wraps another selector, but "slices" the instructions
/// it can see, limiting its scope. This allows for more precise targeting.
/// A common case would be a specific invocation of a repeatedly called method.
public abstract sealed class SlicedSelector<S> permits SlicedPointSelector, SlicedExpressionSelector {
	public final PointSelector from;
	public final PointSelector to;
	public final S selector;

	protected SlicedSelector(PointSelector from, PointSelector to, S selector) {
		this.from = from;
		this.to = to;
		this.selector = selector;
	}

	protected final TransformableCode slice(TransformableCode code) throws TransformException {
		Collection<Point> fromPoints = this.from.find(code);
		if (fromPoints.size() != 1) {
			throw new TransformException("Slice start must match exactly 1 point, got " + fromPoints.size());
		}

		Collection<Point> toPoints = this.to.find(code);
		if (toPoints.size() != 1) {
			throw new TransformException("Slice end must match exactly 1 point, got " + toPoints.size());
		}

		Point start = fromPoints.iterator().next();
		Point end = toPoints.iterator().next();

		InstructionHolder<?> from = start.instruction();
		boolean includeFrom = start.offset() == Offset.BEFORE;
		InstructionHolder<?> to = end.instruction();
		boolean includeTo = end.offset() == Offset.AFTER;

		NavigableSet<InstructionHolder<?>> instructions = code.instructions().subSet(from, includeFrom, to, includeTo);
		return new SlicedTransformableCode(code, instructions);
	}

	protected static <S, T extends SlicedSelector<S>> DualCodec<T> codec(Codec<S> selectorCodec, Factory<S, T> factory) {
		return CompositeCodec.of(
				PointSelector.CODEC.optional(HeadPointSelector.INSTANCE).fieldOf("from"), selector -> selector.from,
				PointSelector.CODEC.optional(TailPointSelector.INSTANCE).fieldOf("to"), selector -> selector.to,
				selectorCodec.fieldOf("selector"), selector -> selector.selector,
				factory::create
		);
	}

	@FunctionalInterface
	protected interface Factory<S, T extends SlicedSelector<S>> {
		T create(PointSelector from, PointSelector to, S selector);
	}
}
