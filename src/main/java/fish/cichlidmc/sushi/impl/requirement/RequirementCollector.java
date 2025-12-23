package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RequirementCollector {
	private final Map<Id, Requirements.Owned> requirements = new LinkedHashMap<>();

	public void add(PreparedTransform transform, Requirement requirement) {
		this.listFor(transform).add(requirement);
	}

	public Requirements build() {
		return Requirements.of(this.requirements.values());
	}

	private List<Requirement> listFor(PreparedTransform transform) {
		return this.requirements.computeIfAbsent(
				transform.owner.id(), id -> new Requirements.Owned(id, new ArrayList<>())
		).requirements();
	}
}
