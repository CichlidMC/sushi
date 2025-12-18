package fish.cichlidmc.sushi.api.requirement.interpreter.exception;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreter;

/// An exception that may occur while interpreting a [Requirement] via a [RequirementInterpreter].
public abstract sealed class RequirementInterpretationException extends Exception permits MalformedRequirementsException, MissingInterpreterException, UnmetRequirementException {
	protected RequirementInterpretationException(String message) {
		super(message);
	}
}
