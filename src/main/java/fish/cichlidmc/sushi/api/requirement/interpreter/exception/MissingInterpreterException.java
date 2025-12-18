package fish.cichlidmc.sushi.api.requirement.interpreter.exception;

import fish.cichlidmc.sushi.api.registry.Id;

/// Indicates that a requirement couldn't be interpreted because no interpreter was registered for it.
public final class MissingInterpreterException extends RequirementInterpretationException {
	public final Id type;

	public MissingInterpreterException(Id type) {
		super("No interpreter registered for requirements of type " + type);
		this.type = type;
	}
}
