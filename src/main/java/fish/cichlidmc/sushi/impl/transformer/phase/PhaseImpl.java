package fish.cichlidmc.sushi.impl.transformer.phase;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.RegisteredTransformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

public final class PhaseImpl implements Phase {
	private final Id id;
	private final NavigableMap<Id, RegisteredTransformer> transformers;
	public final Barriers barriers;

	public PhaseImpl(Id id, NavigableSet<ConfiguredTransformer> transformers, Condition.Context context, Barriers barriers) {
		this.id = id;
		this.barriers = barriers;

		NavigableMap<Id, RegisteredTransformer> transformersMap = new TreeMap<>();

		for (ConfiguredTransformer transformer : transformers) {
			boolean enabled = transformer.condition().map(condition -> condition.test(context)).orElse(true);
			RegisteredTransformer registered = new RegisteredTransformer(transformer, this, enabled);
			transformersMap.put(transformer.id(), registered);
		}

		this.transformers = Collections.unmodifiableNavigableMap(transformersMap);
	}

	@Override
	public Id id() {
		return this.id;
	}

	@Override
	public NavigableMap<Id, RegisteredTransformer> transformers() {
		return this.transformers;
	}

	@Override
	public Barriers barriers() {
		return this.barriers;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PhaseImpl that && this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@NotNull
	@Override
	public String toString() {
		return this.id.toString();
	}
}
