package fish.cichlidmc.sushi.impl.transformer.phase;

import fish.cichlidmc.fishflakes.api.DirectedGraph;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PhaseBuilderImpl extends MutablePhaseImpl implements Phase.Builder {
	private final Set<Id> runsBefore;
	private final Set<Id> runsAfter;
	private Phase.Barriers barriers;

	public PhaseBuilderImpl(Id id, Map<Id, ConfiguredTransformer> allTransformers) {
		super(id, allTransformers);
		this.runsBefore = new HashSet<>();
		this.runsAfter = new HashSet<>();
		this.barriers = Phase.Barriers.NONE;
	}

	@Override
	public Phase.Builder runBefore(Id phase) {
		this.runsBefore.add(phase);
		return this;
	}

	@Override
	public Phase.Builder runAfter(Id phase) {
		this.runsAfter.add(phase);
		return this;
	}

	@Override
	public Phase.Builder withBarriers(Phase.Barriers barriers) {
		this.barriers = barriers;
		return this;
	}

	@Override
	protected Phase.Barriers barriers() {
		return this.barriers;
	}

	public void addToGraph(DirectedGraph<Id> graph) {
		graph.addNode(this.id);

		// all phases that this phase must run before
		for (Id dependent : this.runsBefore) {
			graph.addEdge(this.id, dependent);
		}

		// all phases that this phase must run after
		for (Id dependency : this.runsAfter) {
			graph.addEdge(dependency, this.id);
		}
	}
}
