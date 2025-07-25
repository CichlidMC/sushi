package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Context about the currently occurring transformation.
 */
@ApiStatus.NonExtendable
public interface TransformContext {
	/**
	 * @return the {@link TransformableClass} currently being transformed.
	 */
	TransformableClass clazz();

	/**
	 * Create a new {@link UniqueMethodGenerator} for adding unique methods to the target class.
	 * @param prefix a String to prefix each generated method's name with
	 */
	UniqueMethodGenerator createMethodGenerator(String prefix);

	Optional<Validation> validation();

	/**
	 * @return the {@link Id} of the transformer currently being applied.
	 */
	Id transformerId();
}
