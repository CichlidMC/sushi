package fish.cichlidmc.sushi.impl.condition;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;

import java.util.Collections;
import java.util.Set;

public record ConditionContextImpl(Set<Id> transformers) implements Condition.Context {
	public ConditionContextImpl(Set<Id> transformers) {
		this.transformers = Collections.unmodifiableSet(transformers);
	}
}
