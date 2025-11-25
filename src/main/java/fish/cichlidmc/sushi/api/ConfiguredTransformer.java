package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.Transformer;

import java.util.Optional;

/**
 * A {@link Transformer} that has been assigned an {@link Id} and given additional application information.
 */
public record ConfiguredTransformer(Id id, Transformer transformer, Optional<Condition> condition, int priority, int phase) implements Comparable<ConfiguredTransformer> {
	@Override
	public int compareTo(ConfiguredTransformer that) {
		// first compare by phase
		int phase = Integer.compare(this.phase, that.phase);
		if (phase != 0)
			return phase;

		// then priority
		int priority = Integer.compare(this.priority, that.priority);
		if (priority != 0)
			return priority;

		// fallback to ID
		return this.id.compareTo(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ConfiguredTransformer that && this.id.equals(that.id);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}
}
