package fish.cichlidmc.sushi.api.requirement.interpreter;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.impl.requirement.RequirementStackImpl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/// A stack of requirements that have previously been checked and interpreted.
/// Provides utilities for finding other relevant requirements.
///
/// Consider interpreting a [MethodRequirement] chained after a [ClassRequirement] in a runtime context.
/// In this case, the [MethodRequirement] needs to retrieve the [Class] interpreted from the [ClassRequirement].
/// This can be done by querying the stack for the highest previous [ClassRequirement], since later requirements are higher on the stack.
public sealed interface RequirementStack permits RequirementStackImpl {
	/// @return an immutable List view of this stack
	List<InterpretedRequirement> asList();

	Optional<InterpretedRequirement> findHighest(Id type);

	Optional<InterpretedRequirement> findHighest(Predicate<InterpretedRequirement> predicate);
}
