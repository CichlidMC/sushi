package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.transformer.DirectTransform;

public interface DirectlyTransformable<T extends DirectTransform> {
	/// Apply a transform directly, skipping Sushi's safety.
	///
	/// **Be careful here!** It's very easy to mess something up with this.
	/// Should only be used as a last-resort escape-hatch.
	///
	/// Direct transforms are applied right at the end of transformation,
	/// after Sushi has done everything else.
	void transformDirect(T transform);
}
