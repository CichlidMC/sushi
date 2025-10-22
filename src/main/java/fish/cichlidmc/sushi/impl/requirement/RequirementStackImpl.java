package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.InterpretedRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class RequirementStackImpl implements RequirementStack {
	private final List<InterpretedRequirement> list = new ArrayList<>();
	private final List<InterpretedRequirement> reversed = this.list.reversed();

	public Id transformer;

	public void push(InterpretedRequirement requirement) {
		this.list.addLast(requirement);
	}

	public void pop() {
		this.list.removeLast();
	}

	// the returned list can be safely modified
	@Contract("->new")
	public List<Requirement> currentRequirements() {
		List<Requirement> list = new ArrayList<>();
		for (InterpretedRequirement interpreted : this.list) {
			list.add(interpreted.requirement());
		}
		return list;
	}

	@Override
	public List<InterpretedRequirement> asList() {
		return Collections.unmodifiableList(this.list);
	}

	@Override
	public Optional<InterpretedRequirement> findHighest(Id type) {
		return this.findHighest(requirement -> requirement.type().equals(type));
	}

	@Override
	public Optional<InterpretedRequirement> findHighest(Predicate<InterpretedRequirement> predicate) {
		for (InterpretedRequirement requirement : this.reversed) {
			if (predicate.test(requirement)) {
				return Optional.of(requirement);
			}
		}

		return Optional.empty();
	}
}
