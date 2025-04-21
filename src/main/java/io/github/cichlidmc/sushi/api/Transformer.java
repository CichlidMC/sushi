package io.github.cichlidmc.sushi.api;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.util.Id;

import java.util.Optional;
import java.util.Set;

/**
 * A transformer ready for use by Sushi.
 */
public final class Transformer implements Comparable<Transformer> {
	public final Id id;
	public final ClassTarget target;
	public final Transform transform;
	public final int priority;
	private final int phase;

	public Transformer(Id id, ClassTarget target, Transform transform, int priority, int phase) {
		this.id = id;
		this.target = target;
		this.transform = transform;
		this.priority = priority;
		this.phase = phase;
	}

	public Optional<Set<String>> concreteTargets() {
		return this.target.concreteTargets();
	}

	public String describe() {
		return this.transform.describe();
	}

	@Override
	public int compareTo(Transformer that) {
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
		return obj.getClass() == Transformer.class && this.id.equals(((Transformer) obj).id);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}
}
