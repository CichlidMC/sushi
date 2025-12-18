package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.InterpretedRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreter;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementStack;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.RequirementInterpretationException;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RequirementInterpretersImpl implements RequirementInterpreters {
	private final Map<Id, RequirementInterpreter<?, ?>> map = new HashMap<>();

	@Override
	public <RequirementType extends Requirement, ResultType> void register(Id type, RequirementInterpreter<RequirementType, ResultType> interpreter) throws IllegalArgumentException {
		RequirementInterpreter<?, ?> existing = this.map.get(type);
		if (existing != null) {
			throw new IllegalArgumentException("An interpreter is already registered for " + type + ": " + existing);
		}

		this.map.put(type, interpreter);
	}

	@Nullable
	@Override
	public RequirementInterpreter<?, ?> get(Id type) {
		return this.map.get(type);
	}

	@Nullable
	@Override
	public RequirementInterpreter<?, ?> get(Requirement requirement) throws IllegalStateException {
		Optional<Id> id = typeOf(requirement);
		if (id.isEmpty()) {
			throw new IllegalStateException("Type of requirement is not registered: " + requirement);
		}

		return this.map.get(id.get());
	}


	@Override
	@Nullable
	@SuppressWarnings({"unchecked", "rawtypes"})
	public InterpretedRequirement interpret(Requirement requirement, RequirementStack stack) throws IllegalStateException, RequirementInterpretationException {
		RequirementInterpreter<?, ?> interpreter = this.get(requirement);
		if (interpreter == null)
			return null;

		Object interpreted = ((RequirementInterpreter) interpreter).interpret(requirement, stack);

		Id id = typeOf(requirement).orElseThrow();
		return new InterpretedRequirement(id, requirement, interpreted);
	}

	private static Optional<Id> typeOf(Requirement requirement) {
		return Optional.ofNullable(Requirement.REGISTRY.getId(requirement.codec()));
	}
}
