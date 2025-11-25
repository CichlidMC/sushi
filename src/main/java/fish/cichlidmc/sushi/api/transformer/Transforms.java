package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.target.ClassTarget;

/**
 * Records {@link Transform}s to apply to sets of classes.
 */
@FunctionalInterface
public interface Transforms {
	/**
	 * Register a new transform.
	 * @param target a target defining all classes that the transform should apply to
	 */
	void register(ClassTarget target, Transform transform);
}
