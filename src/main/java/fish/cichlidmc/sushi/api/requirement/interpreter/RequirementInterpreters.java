package fish.cichlidmc.sushi.api.requirement.interpreter;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.RequirementInterpretationException;
import fish.cichlidmc.sushi.impl.requirement.RequirementInterpretersImpl;
import fish.cichlidmc.sushi.impl.requirement.RuntimeRequirementInterpreters;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/// A set of [RequirementInterpreter]s that can be used to check a set of [Requirements].
/// @see Requirements#check(RequirementInterpreters)
public sealed interface RequirementInterpreters permits RequirementInterpretersImpl {
	/// Register an interpreter for requirements with the given type.
	/// @throws IllegalArgumentException if an interpreter has already been registered for this type
	<RequirementType extends Requirement, ResultType> void register(Id type, RequirementInterpreter<RequirementType, ResultType> interpreter) throws IllegalArgumentException;

	/// @return the interpreter for requirements of the given type. May be null.
	@Nullable
	RequirementInterpreter<?, ?> get(Id type);

	/// Shortcut that looks up the type of the given requirement.
	/// @throws IllegalStateException if the requirement is not of a registered type
	@Nullable
	RequirementInterpreter<?, ?> get(Requirement requirement) throws IllegalStateException;

	/// Attempt to interpret the given requirement.
	/// @return the interpreted requirement, or null if no interpreter is registered for this requirement's type
	/// @throws IllegalStateException if the requirement is not of a registered type
	/// @throws RequirementInterpretationException if interpretation of the requirement fails
	@Nullable
	InterpretedRequirement interpret(Requirement requirement, RequirementStack stack) throws IllegalStateException, RequirementInterpretationException;

	/// @return a new, empty set of interpreters
	static RequirementInterpreters create() {
		return new RequirementInterpretersImpl();
	}

	/// Create a new set of interpreters, pre-filled with interpreters for all of Sushi's built-in requirements. This set
	/// of interpreters will interpret requirements as reflective representations, such as [Class] and [Method].
	/// @see MethodHandles#lookup()
	static RequirementInterpreters forRuntime(MethodHandles.Lookup lookup) {
		RequirementInterpreters interpreters = create();
		new RuntimeRequirementInterpreters(lookup).setup(interpreters);
		return interpreters;
	}
}
