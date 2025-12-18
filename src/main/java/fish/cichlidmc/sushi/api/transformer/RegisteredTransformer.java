package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import org.jetbrains.annotations.NotNull;

/// A transformer that has been registered to a [TransformerManager].
/// @param phase the phase that this transformer belongs to
/// @param isEnabled true if this transformer is enabled, determined by its [conditions][ConfiguredTransformer#condition()]
public record RegisteredTransformer(ConfiguredTransformer configured, Phase phase, boolean isEnabled) implements Comparable<RegisteredTransformer> {
	public Id id() {
		return this.configured.id();
	}

	@Override
	public int compareTo(RegisteredTransformer that) {
		return this.configured.compareTo(that.configured);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof RegisteredTransformer that && this.configured.equals(that.configured);
	}

	@Override
	public int hashCode() {
		return this.configured.hashCode();
	}

	@NotNull
	@Override
	public String toString() {
		return this.configured.toString();
	}
}
