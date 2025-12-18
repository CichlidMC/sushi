package fish.cichlidmc.sushi.api.requirement.interpreter;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.RequirementInterpretationException;

/// A RequirementInterpreter is responsible for taking a [Requirement] and "interpreting" it in some way, validating it in the process.
///
/// For example, consider the case of checking a [ClassRequirement] in a runtime context. The [desc][ClassRequirement#desc()]
/// should be resolved into a [Class], which chained requirements may want to query for further information.
@FunctionalInterface
public interface RequirementInterpreter<RequirementType extends Requirement, Result> {
	Result interpret(RequirementType requirement, RequirementStack previous) throws RequirementInterpretationException;
}
