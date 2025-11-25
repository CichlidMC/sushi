package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.requirement.interpreter.InterpretedRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.MissingInterpreterException;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.RequirementInterpretationException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public record RequirementsImpl(List<Requirements.Owned> owned) implements Requirements {
	public static final Codec<Requirements> CODEC = Owned.CODEC.codec().listOf().xmap(
			RequirementsImpl::new, requirements -> ((RequirementsImpl) requirements).owned()
	);

	public static final Requirements EMPTY = new RequirementsImpl(List.of());

	@Override
	public Iterator<Requirements.Owned> iterator() {
		return this.owned.iterator();
	}

	@Override
	public boolean isEmpty() {
		return this.owned.isEmpty();
	}

	@Override
	public Requirements and(Requirements requirements) {
		List<Requirements.Owned> list = new ArrayList<>(this.owned);
		list.addAll(((RequirementsImpl) requirements).owned());
		return new RequirementsImpl(Collections.unmodifiableList(list));
	}

	@Override
	public List<Problem> check(RequirementInterpreters interpreters) {
		if (this.isEmpty()) {
			return List.of();
		}

		RequirementStackImpl stack = new RequirementStackImpl();
		List<Problem> problems = new ArrayList<>();

		for (Requirements.Owned owned : this) {
			stack.transformer = owned.owner();

			for (Requirement root : owned.requirements()) {
				check(root, interpreters, stack, problems);
			}
		}

		return problems;
	}

	private static void check(Requirement requirement, RequirementInterpreters interpreters, RequirementStackImpl stack, List<Problem> problems) {
		try {
			InterpretedRequirement interpreted = interpreters.interpret(requirement, stack);
			if (interpreted == null) {
				Id id = Requirement.REGISTRY.getId(requirement.codec());
				Objects.requireNonNull(id, "id");
				throw new MissingInterpreterException(id);
			}

			stack.push(interpreted);

			for (Requirement chained : requirement.chained()) {
				check(chained, interpreters, stack, problems);
			}

			stack.pop();
		} catch (RequirementInterpretationException exception) {
			List<Requirement> requirements = stack.currentRequirements();
			requirements.add(requirement);
			problems.add(new Problem(stack.transformer, requirements, exception));
		}
	}
}
