package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.TransformerManagerImpl;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Main interface for users of Sushi on the transforming side.
 */
public sealed interface TransformerManager permits TransformerManagerImpl {
	static TransformerManager.Builder builder() {
		return new TransformerManagerImpl.BuilderImpl();
	}

	/**
	 * Transform the given class bytes.
	 * @param context the context to use for parsing and transforming
	 * @param desc the class's desc if known, otherwise will be parsed from the bytes
	 * @param transform an optional additional transform to apply once Sushi is done transforming
	 * @return a {@link TransformResult} if a transformation was applied, otherwise empty
	 * @throws IllegalArgumentException if the class bytes are malformed
	 * @throws TransformException if an error occurs while transforming
	 */
	Optional<TransformResult> transform(ClassFile context, byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) throws TransformException;

	default Optional<TransformResult> transform(byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) throws TransformException {
		return this.transform(ClassFile.of(), bytes, desc, transform);
	}

	default Optional<TransformResult> transform(byte[] bytes, @Nullable ClassDesc desc) throws TransformException {
		return this.transform(bytes, desc, null);
	}

	/**
	 * @return an immutable view of the registered transformers
	 */
	Map<Id, ConfiguredTransformer> transformers();

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
		boolean register(ConfiguredTransformer transformer);

		/**
		 * Register a new transformer, throwing an exception if one with that ID already exists.
		 * @throws IllegalArgumentException if a transformer with the given ID is already registered
		 */
		Builder registerOrThrow(ConfiguredTransformer transformer) throws IllegalArgumentException;

		/**
		 * Modify the attachments on the {@link Condition.Context}
		 * that will be used to determine which transformers to load.
		 */
		Builder configureConditionContext(Consumer<AttachmentMap> consumer);

		/**
		 * Determine if metadata should be added to transformed classes.
		 * Defaults to true if not set explicitly.
		 */
		Builder addMetadata(boolean value);

		/**
		 * Build a new manager will all registered transformers.
		 */
		TransformerManager build();
	}
}
