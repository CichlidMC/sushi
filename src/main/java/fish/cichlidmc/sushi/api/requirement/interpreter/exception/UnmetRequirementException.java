package fish.cichlidmc.sushi.api.requirement.interpreter.exception;

/**
 * An exception indicating that a requirement was unmet for whatever reason.
 */
public final class UnmetRequirementException extends RequirementInterpretationException {
	public UnmetRequirementException(String message) {
		super(message);
	}
}
