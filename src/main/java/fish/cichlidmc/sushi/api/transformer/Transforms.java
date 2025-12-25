package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;

/// Records [Transform]s to apply to sets of classes.
@FunctionalInterface
public interface Transforms {
	/// Register a new transform.
	/// @param predicate a [ClassPredicate] defining all classes that the transform should apply to
	void register(ClassPredicate predicate, Transform transform);
}
