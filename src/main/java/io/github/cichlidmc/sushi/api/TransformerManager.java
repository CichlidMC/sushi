package io.github.cichlidmc.sushi.api;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.impl.TransformerManagerImpl;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Main interface for users of Sushi on the transforming side.
 * Register transformers and targets, and transform classes.
 */
@ApiStatus.NonExtendable
public interface TransformerManager {
	/**
	 * Run the given class through all registered transformers.
	 * The ClassReader that read the given ClassNode may optionally be provided to allow ASM to perform optimizations while writing.
	 * @return true if a transformation occurred
	 * @throws TransformException if any transformer throws one
	 */
	boolean transform(ClassNode node, @Nullable ClassReader reader) throws TransformException;

	static TransformerManager.Builder builder() {
		return new TransformerManagerImpl.BuilderImpl();
	}

	@ApiStatus.NonExtendable
	interface Builder {
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
		 * Set the output path for exporting transformed classes.
		 * If not set, classes will never be written to the filesystem by Sushi.
		 */
		Builder output(Path path);

		/**
		 * Determine if {@link SushiMetadata} should be added to transformed classes.
		 * Defaults to true.
		 */
		Builder addMetadata(boolean value);

		/**
		 * Build a new manager will all registered transformers.
		 */
		TransformerManager build();
	}
}
