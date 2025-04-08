package io.github.cichlidmc.sushi.api;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import org.objectweb.asm.tree.ClassNode;

import java.util.Optional;
import java.util.Set;

/**
 * A transformer ready for use by Sushi.
 */
public final class Transformer implements Comparable<Transformer> {
	public final Id id;

	private final ClassTarget target;
	private final Transform transform;
	private final int priority;
	private final int phase;

	public Transformer(Id id, ClassTarget target, Transform transform, int priority, int phase) {
		this.id = id;
		this.target = target;
		this.transform = transform;
		this.priority = priority;
		this.phase = phase;
	}

	public Transformer(Id id, ClassTarget target, Transform transform) {
		this(id, target, transform, 0, 0);
	}

	public boolean apply(ClassNode node) throws TransformException {
		if (!this.target.shouldApply(node))
			return false;

		try {
			return this.transform.apply(node);
		} catch (TransformException e) {
			throw new TransformException("Error applying transformer " + this.id + " to class " + node.name, e);
		} catch (Throwable t) {
			throw new TransformException(
					"An unhandled exception occurred while applying transformer " + this.id + " to class " + node.name, t
			);
		}
	}

	public Optional<Set<String>> concreteTargets() {
		return this.target.concreteTargets();
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
