package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.api.transformer.phase.PhaseCycleException;
import fish.cichlidmc.sushi.impl.TransformerManagerImpl;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Consumer;

/// Main interface for users of Sushi on the transforming side.
public sealed interface TransformerManager permits TransformerManagerImpl {
	static TransformerManager.Builder builder() {
		return new TransformerManagerImpl.BuilderImpl();
	}

	/// Transform the given class bytes.
	/// @param context the context to use for parsing and transforming
	/// @param desc the class's desc if known, otherwise will be parsed from the bytes
	/// @param transform an optional additional transform to apply once Sushi is done transforming
	/// @return a [TransformResult] if a transformation was applied, otherwise empty
	/// @throws IllegalArgumentException if the class bytes are malformed
	/// @throws TransformException if an error occurs while transforming
	Optional<TransformResult> transform(ClassFile context, byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) throws TransformException;

	default Optional<TransformResult> transform(byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) throws TransformException {
		return this.transform(ClassFile.of(), bytes, desc, transform);
	}

	default Optional<TransformResult> transform(byte[] bytes, @Nullable ClassDesc desc) throws TransformException {
		return this.transform(ClassFile.of(), bytes, desc, null);
	}

	/// @return an immutable view of all registered transformers
	Map<Id, ConfiguredTransformer> transformers();

	/// @return an immutable view of all registered phases, in application order
	SequencedMap<Id, Phase> phases();

	sealed interface Builder permits TransformerManagerImpl.BuilderImpl {
		/// Get the default phase.
		Phase.Mutable defaultPhase();

		/// Define a new [Phase] that transformers can be registered to.
		/// @return a builder for the phase, or empty if a phase with that ID is already defined
		/// @throws IllegalArgumentException if the given ID is that of the [default phase][Phase#DEFAULT]
		Optional<Phase.Builder> definePhase(Id id) throws IllegalArgumentException;

		/// Try to define a [Phase] with the given ID.
		/// @throws IllegalArgumentException if a phase with that ID is already defined
		/// @see #definePhase(Id)
		Phase.Builder definePhaseOrThrow(Id id) throws IllegalArgumentException;

		/// Modify the attachments on the [Condition.Context]
		/// that will be used to determine which transformers to load.
		Builder configureConditionContext(Consumer<AttachmentMap> consumer);

		/// Determine if metadata should be added to transformed classes.
		/// Defaults to true if not set explicitly.
		Builder addMetadata(boolean value);

		/// Build a new manager will all registered transformers.
		/// @throws PhaseCycleException if any registered phases create a circular dependency chain
		TransformerManager build() throws PhaseCycleException;
	}
}
