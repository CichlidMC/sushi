package fish.cichlidmc.sushi.api.transformer.phase;

import fish.cichlidmc.sushi.api.registry.Id;

import java.util.SequencedSet;
import java.util.StringJoiner;

/// Thrown when registered phases form a cyclical dependency chain.
public final class PhaseCycleException extends RuntimeException {
	/// The set of phases that form a cycle. The last phase depends on the first one, completing the cycle.
	public final SequencedSet<Id> phases;

	public PhaseCycleException(SequencedSet<Id> phases) {
		super("Phases form a cycle: " + formatPhases(phases));
		this.phases = phases;
	}

	private static String formatPhases(SequencedSet<Id> phases) {
		StringJoiner joiner = new StringJoiner(" -> ", "[", "]");
		phases.forEach(phase -> joiner.add(phase.toString()));

		// add the first one again to complete the loop
		joiner.add(phases.getFirst().toString());

		return joiner.toString();
	}
}
