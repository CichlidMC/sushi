package fish.cichlidmc.sushi.api.requirement.interpreter;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.MalformedRequirementsException;

/**
 * A requirement and its interpretation, as provided by a {@link RequirementInterpreter}.
 * @param type the type of the requirement
 */
public record InterpretedRequirement(Id type, Requirement requirement, Object interpreted) {
	/**
	 * Get the interpreted value of this requirement, but check that the type is as expected.
	 * @throws MalformedRequirementsException if the type does not match
	 */
	public <T> T getChecked(Class<T> expectedType) throws MalformedRequirementsException {
		if (expectedType.isInstance(this.interpreted)) {
			return expectedType.cast(this.interpreted);
		} else {
			throw new MalformedRequirementsException(String.format(
					"Type of interpreted value (%s) does not match expected type (%s)",
					this.interpreted.getClass(), expectedType
			));
		}
	}

	public boolean is(Id type) {
		return this.type.equals(type);
	}
}
