package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;

import java.util.Optional;

/**
 * A {@link Transformer} that has been assigned an {@link Id} and given additional application information.
 */
public record ConfiguredTransformer(Id id, Transformer transformer, Optional<Condition> condition) implements Comparable<ConfiguredTransformer> {
	public ConfiguredTransformer(Id id, Transformer transformer) {
		this(id, transformer, Optional.empty());
	}

	@Override
	public int compareTo(ConfiguredTransformer that) {
		return this.id.compareTo(that.id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ConfiguredTransformer that && this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return this.id.toString();
	}
}
