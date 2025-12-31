package fish.cichlidmc.sushi.api.match.point;

import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.util.Collection;

/// A [Target] for [points][PointSelector].
public record PointTarget(PointSelector selector, int expected) implements Target {
	public static final DualCodec<PointTarget> CODEC = CompositeCodec.of(
			PointSelector.CODEC.fieldOf("selector"), PointTarget::selector,
			EXPECTED_CODEC.fieldOf("expected"), PointTarget::expected,
			PointTarget::new
	);

	public PointTarget {
		Target.checkExpected(expected);
	}

	public PointTarget(PointSelector selector) {
		this(selector, DEFAULT_EXPECTATION);
	}

	/// Find all matching points in the given code.
	/// @throws TransformException if the number of found points does not match this target
	public Collection<Point> find(TransformableCode code) throws TransformException {
		Collection<Point> found = this.selector.find(code);
		if (found.isEmpty()) {
			throw new TransformException("No matching points were found");
		} else if (this.isUnlimited() || found.size() == this.expected) {
			return found;
		}

		throw new TransformException(String.format(
				"Found %d matching point(s), but expected %d",
				found.size(), this.expected
		));
	}
}
