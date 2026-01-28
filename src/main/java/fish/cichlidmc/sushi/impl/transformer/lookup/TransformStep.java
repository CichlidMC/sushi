package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.StringJoiner;

public record TransformStep(SequencedMap<Phase, SequencedSet<PreparedTransform>> phases) {
	public SequencedSet<PreparedTransform> transforms() {
		SequencedSet<PreparedTransform> set = new LinkedHashSet<>();
		this.phases.values().forEach(set::addAll);
		return Collections.unmodifiableSequencedSet(set);
	}

	public boolean contains(PreparedTransform transform) {
		SequencedSet<PreparedTransform> inPhase = this.phases.get(transform.owner.phase());
		return inPhase != null && inPhase.contains(transform);
	}

	// format: mymod:phase[mymod:a], default[-> mymod:b <-, mymod:c]
	public String toDetail(PreparedTransform current) {
		StringJoiner joiner = new StringJoiner(", ");
		this.phases.forEach((phase, transforms) -> joiner.add(toDetail(phase, transforms, current)));
		return joiner.toString();
	}

	private static String toDetail(Phase phase, SequencedSet<PreparedTransform> transforms, PreparedTransform current) {
		StringJoiner joiner = new StringJoiner(", ", nameOf(phase) + '[', "]");

		for (PreparedTransform transform : transforms) {
			Id id = transform.owner.id();

			if (transform == current) {
				joiner.add("-> " + id + " <-");
			} else {
				joiner.add(id.toString());
			}
		}

		return joiner.toString();
	}

	private static String nameOf(Phase phase) {
		return phase.id().equals(Phase.DEFAULT) ? "default" : phase.id().toString();
	}
}
