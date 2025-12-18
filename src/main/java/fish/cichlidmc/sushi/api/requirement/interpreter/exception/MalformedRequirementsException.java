package fish.cichlidmc.sushi.api.requirement.interpreter.exception;

/// Indicates that the structure of the requirements being checked is malformed in some way.
/// For example, attempting to check a requirement that needs some context without that context.
public final class MalformedRequirementsException extends RequirementInterpretationException {
	public MalformedRequirementsException(String message) {
		super(message);
	}
}
