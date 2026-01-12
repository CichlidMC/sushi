package fish.cichlidmc.sushi.api.match.field;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;

import java.util.Collection;

/// A [Target] for [fields][FieldSelector].
public record FieldTarget(FieldSelector selector, int expected) implements Target {
	public static final Codec<FieldTarget> CODEC = Target.codec(
			FieldSelector.CODEC, FieldTarget::new, FieldTarget::selector
	);

	public FieldTarget {
		Target.checkExpected(expected);
	}

	public FieldTarget(FieldSelector selector) {
		this(selector, DEFAULT);
	}

	/// Find all matching fields in the given class.
	/// @throws TransformException if the number of found fields does not match this target
	public Collection<TransformableField> find(TransformableClass clazz) throws TransformException {
		Collection<TransformableField> found = this.selector.find(clazz);

		try {
			Target.checkFound(this, found);
		} catch (TransformException e) {
			Details details = e.details();
			for (TransformableField match : found) {
				details.add("Match", match);
			}
			throw e;
		}

		return found;
	}
}
