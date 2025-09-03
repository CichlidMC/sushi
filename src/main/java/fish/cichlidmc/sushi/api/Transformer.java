package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.util.Id;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/**
 * A transformer ready for use by Sushi.
 */
public record Transformer(Id id, ClassTarget target, Transform transform, int priority, int phase) implements Comparable<Transformer> {

	public boolean shouldApply(ClassModel model) {
		return this.target.shouldApply(model);
	}

	public Optional<Set<ClassDesc>> concreteTargets() {
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
