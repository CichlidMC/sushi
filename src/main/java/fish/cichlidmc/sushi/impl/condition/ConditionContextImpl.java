package fish.cichlidmc.sushi.impl.condition;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;

import java.util.Set;

public record ConditionContextImpl(Set<Id> transformers, AttachmentMap attachments) implements Condition.Context {
	public ConditionContextImpl(Set<Id> transformers, AttachmentMap attachments) {
		this.transformers = transformers;
		this.attachments = attachments.immutable();
	}
}
