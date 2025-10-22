package fish.cichlidmc.sushi.api.requirement.interpreter;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.impl.requirement.RequirementStackImpl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A stack of requirements that have previously been checked and interpreted.
 * Provides utilities for finding other relevant requirements.
 * <p>
 * Consider interpreting a {@link MethodRequirement} chained after a {@link ClassRequirement} in a runtime context.
 * In this case, the {@link MethodRequirement} needs to retrieve the {@link Class} interpreted from the {@link ClassRequirement}.
 * This can be done by querying the stack for the highest previous {@link ClassRequirement}, since later requirements are higher on the stack.
 */
public sealed interface RequirementStack permits RequirementStackImpl {
	/**
	 * @return an immutable List view of this stack
	 */
	List<InterpretedRequirement> asList();

	Optional<InterpretedRequirement> findHighest(Id type);

	Optional<InterpretedRequirement> findHighest(Predicate<InterpretedRequirement> predicate);
}
