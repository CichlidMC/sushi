package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transformer.Transform;

public record PreparedTransform(ConfiguredTransformer owner, ClassTarget target, Transform transform) implements Comparable<PreparedTransform> {
	@Override
	public int compareTo(PreparedTransform that) {
		return this.owner.compareTo(that.owner);
	}
}
