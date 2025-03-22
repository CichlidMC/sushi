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
	 * The ClassReader that read the given ClassNode may optionally be provided to allow ASM to perform optimizations.
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
		 * Register a transformer that will be parsed from the given JSON.
		 * @return an optional error message if the given JSON is not a valid transformer, or if that ID is already in use
		 */
		Optional<String> parseAndRegister(Id id, JsonValue json);

		/**
		 * Set the output path for exporting transformed classes.
		 * Defaults to null, which means no classes will be written.
		 */
		Builder output(Path path);

		/**
		 * Build a new manager will all registered transformers.
		 */
		TransformerManager build();
	}
}
