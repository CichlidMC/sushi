package fish.cichlidmc.sushi.api.transformer.infra;

import fish.cichlidmc.sushi.api.model.code.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.transformer.slice.SlicedTransformableCode;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;

import java.util.Collection;
import java.util.NavigableSet;

/// Describes a range of instructions in a method's bytecode.
public record Slice(InjectionPoint from, InjectionPoint to) {
	/// A Slice that doesn't do anything, since it includes the entire method.
	public static final Slice NONE = new Slice(HeadInjectionPoint.INSTANCE, TailInjectionPoint.INSTANCE);

	public static final Codec<Slice> CODEC = CompositeCodec.of(
			InjectionPoint.CODEC.optional(HeadInjectionPoint.INSTANCE).fieldOf("from"), Slice::from,
			InjectionPoint.CODEC.optional(TailInjectionPoint.INSTANCE).fieldOf("to"), Slice::to,
			Slice::new
	).codec();

	/// Codec that defaults to [#NONE] when not present.
	public static final Codec<Slice> DEFAULTED_CODEC = CODEC.optional(NONE);

	/// Resolve this Slice, finding the exact start and end points for the given code.
	/// @throws TransformException if this slice is malformed, which occurs in any of these cases:
	/// - Not matching exactly one start point
	/// - Not matching exactly one end point
	/// - Matched end point comes before matched start point
	public Resolved resolve(TransformableCode code) throws TransformException {
		Collection<Point> from = this.from.find(code);
		if (from.size() != 1) {
			throw new TransformException("Slice start must match exactly 1 point, got " + from.size());
		}

		Collection<Point> to = this.to.find(code);
		if (to.size() != 1) {
			throw new TransformException("Slice end must match exactly 1 point, got " + to.size());
		}

		return new Resolved(from.iterator().next(), to.iterator().next());
	}

	/// @return a sub-view of the given code that only includes the instructions that fall within this slice
	/// @throws TransformException if this slice cannot be [resolved][#resolve(TransformableCode)]
	public TransformableCode apply(TransformableCode code) throws TransformException {
		// special case to avoid unnecessary wrapping
		if (this.from == HeadInjectionPoint.INSTANCE && this.to == TailInjectionPoint.INSTANCE)
			return code;

		Slice.Resolved resolved = this.resolve(code);

		InstructionHolder<?> from = resolved.start.instruction();
		boolean includeFrom = resolved.start.offset() == Offset.BEFORE;
		InstructionHolder<?> to = resolved.end.instruction();
		boolean includeTo = resolved.end.offset() == Offset.AFTER;

		NavigableSet<InstructionHolder<?>> instructions = code.instructions().subSet(from, includeFrom, to, includeTo);
		return new SlicedTransformableCode(code, instructions);
	}

	public record Resolved(Point start, Point end) {
	}
}
