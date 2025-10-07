package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A parsed transformer JSON, before being split.
 */
public record TransformerDefinition(ClassTarget target, List<Transform> transforms, Optional<Condition> condition, int priority, int phase) {
	public static final Codec<TransformerDefinition> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), def -> def.target,
			Transform.CODEC.listOrSingle().fieldOf("transforms"), def -> def.transforms,
			Condition.CODEC.optional().fieldOf("condition"), def -> def.condition,
			Codec.INT.optional(0).fieldOf("priority"), def -> def.priority,
			Codec.INT.optional(0).fieldOf("phase"), def -> def.phase,
			TransformerDefinition::new
	).asCodec();

	public List<Transformer> decompose(Id baseId) {
		if (this.transforms.size() == 1) {
			Transform transform = this.transforms.getFirst();
			return List.of(new Transformer(baseId, this.target, transform, this.condition, this.priority, this.phase));
		}

		List<Transformer> transformers = new ArrayList<>();

		for (int i = 0; i < this.transforms.size(); i++) {
			Id id = baseId.suffixed("/" + i);
			Transform transform = this.transforms.get(i);
			Transformer transformer = new Transformer(id, this.target, transform, this.condition, this.priority, this.phase);
			transformers.add(transformer);
		}

		return transformers;
	}
}
