package fish.cichlidmc.sushi.api.match.point;

import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;

import java.util.Collection;

/// A [Target] for [points][PointSelector].
public record PointTarget(PointSelector selector, int expected) implements Target {
	public static final Codec<PointTarget> CODEC = Target.codec(
			PointSelector.CODEC, PointTarget::new, PointTarget::selector
	);

	public PointTarget {
		Target.checkExpected(expected);
	}

	public PointTarget(PointSelector selector) {
		this(selector, DEFAULT);
	}

	/// Find all matching points in the given code.
	/// @throws TransformException if the number of found points does not match this target
	public Collection<Point> find(TransformableCode code) throws TransformException {
		Collection<Point> found = this.selector.find(code);
		Target.checkFound(this, found);
		return found;
	}
}
