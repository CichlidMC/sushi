package fish.cichlidmc.sushi.api.match;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import org.jetbrains.annotations.ApiStatus;

/// A target associates a selector with an expected number of matches.
///
/// Targets should always match at least once.
public sealed interface Target permits MethodTarget, ExpressionTarget {
	/// When [#expected()] has this value, then any number of matches are allowed.
	int UNLIMITED = 0;
	/// It's typically desired for a target to match exactly once.
	int DEFAULT_EXPECTATION = 1;

	/// Common codec for a target's expected number of matches.
	///
	/// Optional non-negative integer, [with a default][#DEFAULT_EXPECTATION].
	Codec<Integer> EXPECTED_CODEC = SushiCodecs.NON_NEGATIVE_INT.optional(DEFAULT_EXPECTATION);

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
}
