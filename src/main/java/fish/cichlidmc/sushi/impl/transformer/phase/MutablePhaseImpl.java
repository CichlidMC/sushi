package fish.cichlidmc.sushi.impl.transformer.phase;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;

import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public sealed class MutablePhaseImpl implements Phase.Mutable permits PhaseBuilderImpl {
	protected final Id id;
	// track all transformers regardless of phase to ensure uniqueness
	private final Map<Id, ConfiguredTransformer> allTransformers;
	private final NavigableSet<ConfiguredTransformer> myTransformers;

	public MutablePhaseImpl(Id id, Map<Id, ConfiguredTransformer> allTransformers) {
		this.id = id;
		this.allTransformers = allTransformers;
		this.myTransformers = new TreeSet<>();
	}

	@Override
	public boolean register(ConfiguredTransformer transformer) {
		if (this.allTransformers.containsKey(transformer.id()))
			return false;

		this.allTransformers.put(transformer.id(), transformer);
		this.myTransformers.add(transformer);
		return true;
	}

	@Override
	public void registerOrThrow(ConfiguredTransformer transformer) throws IllegalArgumentException {
		if (!this.register(transformer)) {
			throw new IllegalArgumentException("Duplicate transformers with ID " + transformer.id());
		}
	}

	public Phase build(Condition.Context ctx) {
		return new PhaseImpl(this.id, this.myTransformers, ctx, this.barriers());
	}

	protected Phase.Barriers barriers() {
		return Phase.Barriers.NONE;
	}
}
