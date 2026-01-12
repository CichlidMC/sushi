package fish.cichlidmc.sushi.api.match;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

/// A target associates a selector with an expected number of matches.
///
/// Targets should always match at least once.
public interface Target {
	/// When [#expected()] has this value, then any number of matches are allowed.
	int UNLIMITED = 0;
	/// It's typically desired for a target to match exactly once, so `1` is the default in several places.
	int DEFAULT = 1;
	/// Common codec for a target's expected number of matches.
	///
	/// Optional non-negative integer, [with a default][#DEFAULT].
	Codec<Integer> EXPECTED_CODEC = SushiCodecs.NON_NEGATIVE_INT.optional(DEFAULT);

	/// The expected number of matches. Always non-negative.
	///
	/// A value of 0 indicates that this target may match an unlimited number of times.
	/// @see #UNLIMITED
	int expected();

	/// @return true if this target can match an unlimited number of times
	@ApiStatus.NonExtendable
	default boolean isUnlimited() {
		return this.expected() == UNLIMITED;
	}

	/// Helper method that checks that the given expected number of matches is valid.
	/// @throws IllegalArgumentException if `expected` is less than 0
	static void checkExpected(int expected) throws IllegalArgumentException {
		if (expected < 0) {
			throw new IllegalArgumentException("Targets cannot match a negative number of times");
		}
	}

	/// Helper method that will check that the given collection of
	/// found elements is the correct size based on the given Target.
	///
	/// This is only intended to be called by implementations.
	static void checkFound(Target target, Collection<?> found) throws TransformException {
		Details.with("Target", target, TransformException::new, () -> {
			if (found.isEmpty()) {
				throw new TransformException("Target matched 0 times, expected " + expectedString(target));
			}

			if (!target.isUnlimited() && found.size() != target.expected()) {
				throw new TransformException(String.format(
						"Target found %d match(es), but expected %s",
						found.size(), expectedString(target)
				));
			}
		});
	}

	/// Helper method that creates a [Codec] for a standard Target implementation.
	/// @param <S> the type of the target's wrapped selector
	/// @param selectorCodec a [Codec] for the selector
	/// @param selectorGetter a function that extracts the wrapped selector from a given target
	static <S, T extends Target> Codec<T> codec(Codec<S> selectorCodec, BiFunction<S, Integer, T> factory, Function<T, S> selectorGetter) {
		Codec<T> directCodec = CompositeCodec.of(
				selectorCodec.fieldOf("selector"), selectorGetter::apply,
				EXPECTED_CODEC.fieldOf("expected"), Target::expected,
				factory::apply
		).codec();

		Codec<T> inlineSelectorCodec = selectorCodec.xmap(
				selector -> factory.apply(selector, DEFAULT),
				selectorGetter
		);

		return directCodec.withAlternative(inlineSelectorCodec);
	}

	private static String expectedString(Target target) {
		return target.isUnlimited() ? "<unlimited>" : String.valueOf(target.expected());
	}
}
