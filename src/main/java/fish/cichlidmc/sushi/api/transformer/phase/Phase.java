package fish.cichlidmc.sushi.api.transformer.phase;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.RegisteredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.impl.transformer.phase.MutablePhaseImpl;
import fish.cichlidmc.sushi.impl.transformer.phase.PhaseBuilderImpl;
import fish.cichlidmc.sushi.impl.transformer.phase.PhaseImpl;

import java.util.NavigableMap;

/// A Phase is a set of [Transformer]s that will be applied in sequence.
/// A Phase may be defined to run before or after other phases.
public sealed interface Phase permits PhaseImpl {
	/// The ID of the default phase, which transformers will belong to unless specified otherwise.
	Id DEFAULT = Sushi.id("default");

	/// @return the unique ID of this phase
	Id id();

	/// @return an immutable view of this phase's transformers, in the order that they will be applied. Possibly empty.
	NavigableMap<Id, RegisteredTransformer> transformers();

	/// @return the [Barriers] surrounding this phase
	Barriers barriers();

	/// Barriers mark a break in transformation where all current changes
	/// are finalized before continuing. This means that all transforms following
	/// the barrier will be given a modified target class, not the original version.
	/// This allows transformers to target changes made by others that would normally be invisible.
	///
	/// Each phase may or may not have a barrier on either end of it.
	enum Barriers {
		NONE(false, false),
		BEFORE_ONLY(true, false),
		AFTER_ONLY(false, true),
		BOTH(true, true);

		public final boolean before;
		public final boolean after;

		Barriers(boolean before, boolean after) {
			this.before = before;
			this.after = after;
		}
	}

	/// A mutable Phase, open for transformer registration.
	sealed interface Mutable permits Builder, MutablePhaseImpl {
		/// Attempt to register the given transformer. Fails to do so if a transformer with the same ID is already registered.
		/// @return true if the transformer was successfully registered
		boolean register(ConfiguredTransformer transformer);

		/// Attempt to register the given transformer.
		/// @throws IllegalArgumentException if a transformer with the same ID is already registered
		void registerOrThrow(ConfiguredTransformer transformer) throws IllegalArgumentException;
	}

	/// A builder for a Phase.
	/// In addition to being open for transformer registration, you can also
	/// define other phases to run before or after it,
	sealed interface Builder extends Mutable permits PhaseBuilderImpl {
		/// Define a phase that this phase must run before if it exists.
		Builder runBefore(Id phase);

		/// Define a phase that this phase must run after if it exists.
		Builder runAfter(Id phase);

		/// Set the [Barriers] of this phase.
		/// By default, a phase has no barriers.
		Builder withBarriers(Barriers barriers);
	}
}
