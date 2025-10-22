package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.sushi.impl.transform.sliced.SlicedTransformContext;

/**
 * Context about the currently occurring transformation.
 */
public sealed interface TransformContext permits TransformContextImpl, SlicedTransformContext {
	/**
	 * @return the {@link TransformableClass} currently being transformed.
	 */
	TransformableClass clazz();

	/**
	 * Register a new {@link Requirement} that must be met for transformations to be correct.
	 */
	void require(Requirement requirement);

	/**
	 * @return true if metadata of any kind should be added to transformed classes
	 */
	boolean addMetadata();

	/**
	 * @return the {@link Id} of the transformer currently being applied.
	 */
	Id transformerId();
}
