package fish.cichlidmc.sushi.api.match.expression;

import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.util.Collection;

/// A [Target] for [expressions][ExpressionSelector].
public record ExpressionTarget(ExpressionSelector selector, int expected) implements Target {
	public static final DualCodec<ExpressionTarget> CODEC = CompositeCodec.of(
			ExpressionSelector.CODEC.fieldOf("selector"), ExpressionTarget::selector,
			EXPECTED_CODEC.fieldOf("expected"), ExpressionTarget::expected,
			ExpressionTarget::new
	);

	public ExpressionTarget {
		Target.checkExpected(expected);
	}

	public ExpressionTarget(ExpressionSelector selector) {
		this(selector, DEFAULT_EXPECTATION);
	}

	/// Find all matching expressions in the given code.
	/// @throws TransformException if the number of found expressions does not match this target
	public Collection<ExpressionSelector.Found> find(TransformableCode code) throws TransformException {
		Collection<ExpressionSelector.Found> found = this.selector.find(code);
		if (found.isEmpty()) {
			throw new TransformException("No matching expressions were found");
		} else if (this.isUnlimited() || found.size() == this.expected) {
			return found;
		}

		throw new TransformException(String.format(
				"Found %d matching expression(s), but expected %d",
				found.size(), this.expected
		));
	}
}
