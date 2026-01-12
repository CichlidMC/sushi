package fish.cichlidmc.sushi.api.match.expression;

import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;

import java.util.Collection;

/// A [Target] for [expressions][ExpressionSelector].
public record ExpressionTarget(ExpressionSelector selector, int expected) implements Target {
	public static final Codec<ExpressionTarget> CODEC = Target.codec(
			ExpressionSelector.CODEC, ExpressionTarget::new, ExpressionTarget::selector
	);

	public ExpressionTarget {
		Target.checkExpected(expected);
	}

	public ExpressionTarget(ExpressionSelector selector) {
		this(selector, DEFAULT);
	}

	/// Find all matching expressions in the given code.
	/// @throws TransformException if the number of found expressions does not match this target
	public Collection<ExpressionSelector.Found> find(TransformableCode code) throws TransformException {
		Collection<ExpressionSelector.Found> found = this.selector.find(code);
		Target.checkFound(this, found);
		return found;
	}
}
