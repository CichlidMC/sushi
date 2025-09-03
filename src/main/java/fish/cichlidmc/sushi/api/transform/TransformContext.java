package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.sushi.impl.transform.sliced.SlicedTransformContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Context about the currently occurring transformation.
 */
public sealed interface TransformContext permits TransformContextImpl, SlicedTransformContext {
	/**
	 * @return the {@link TransformableClass} currently being transformed.
	 */
	TransformableClass clazz();

	Optional<Validation> validation();

	/**
	 * @return true if metadata of any kind should be added to transformed classes
	 */
	boolean addMetadata();

	/**
	 * @return the {@link Id} of the transformer currently being applied.
	 */
	Id transformerId();
}
