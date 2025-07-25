package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.TransformerManagerImpl;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Main interface for users of Sushi on the transforming side.
 */
public sealed interface TransformerManager permits TransformerManagerImpl {
	static TransformerManager.Builder builder() {
		return new TransformerManagerImpl.BuilderImpl();
	}

	/**
	 * Create a new {@link ClassTransform} to apply to the given class, if there's any transformations to make.
	 * <p>
	 * The returned {@link ClassTransform} must be invoked first if chained. It relies on the provided
	 * {@link ClassModel} exactly matching its input.
	 */
	Optional<ClassTransform> transformFor(LazyClassModel clazz);

	sealed interface Builder permits TransformerManagerImpl.BuilderImpl {
		/**
		 * Register one or more transformers by parsing the given JSON.
		 * If the JSON contains multiple transformers, all of them are registered together.
		 * If any of them cannot be registered, then none of them will be.
		 * @return an optional error message if the given JSON is invalid or if one or more IDs are already taken
		 */
		Optional<String> parseAndRegister(Id id, JsonValue json);

		/**
		 * Register a new transformer using its ID.
		 * @return true if it was successfully registered, false if that ID is already in use
		 */
		boolean register(Transformer transformer);

		/**
		 * Register a new transformer, throwing an exception if one with that ID already exists.
		 * @throws IllegalArgumentException if a transformer with the given ID is already registered
		 */
		Builder registerOrThrow(Transformer transformer) throws IllegalArgumentException;

		/**
		 * Set the {@link Validation} for transforms to use.
		 * Defaults to none if not set explicitly.
		 */
		Builder withValidation(@Nullable Validation validation);

		/**
		 * Determine if {@link SushiMetadata} should be added to transformed classes.
		 * Defaults to true if not set explicitly.
		 */
		Builder addMetadata(boolean value);

		/**
		 * Build a new manager will all registered transformers.
		 */
		TransformerManager build();
	}
}
