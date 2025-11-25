package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A parsed transformer JSON, before being split.
 */
public record TransformerDefinition(List<Transformer> transformers, Optional<Condition> condition, int priority, int phase) {
	public static final DualCodec<TransformerDefinition> CODEC = CompositeCodec.of(
			Transformer.CODEC.listOrSingle().fieldOf("transforms"), def -> def.transformers,
			Condition.CODEC.optional().fieldOf("condition"), def -> def.condition,
			Codec.INT.optional(0).fieldOf("priority"), def -> def.priority,
			Codec.INT.optional(0).fieldOf("phase"), def -> def.phase,
			TransformerDefinition::new
	);

	public List<ConfiguredTransformer> decompose(Id baseId) {
		if (this.transformers.size() == 1) {
			Transformer transformer = this.transformers.getFirst();
			return List.of(new ConfiguredTransformer(baseId, transformer, this.condition, this.priority, this.phase));
		}

		List<ConfiguredTransformer> transformers = new ArrayList<>();

		for (int i = 0; i < this.transformers.size(); i++) {
			Id id = baseId.suffixed("/" + i);
			Transformer transform = this.transformers.get(i);
			ConfiguredTransformer transformer = new ConfiguredTransformer(id, transform, this.condition, this.priority, this.phase);
			transformers.add(transformer);
		}

		return transformers;
	}
}
