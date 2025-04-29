package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A parsed transformer JSON, before being split.
 */
public final class TransformerDefinition {
	public static final Codec<TransformerDefinition> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), def -> def.target,
			Transform.CODEC.listOrSingle().fieldOf("transforms"), def -> def.transforms,
			Codec.INT.optional(0).fieldOf("priority"), def -> def.priority,
			Codec.INT.optional().fieldOf("phase_override"), def -> def.phaseOverride,
			TransformerDefinition::new
	).asCodec();

	private final ClassTarget target;
	private final List<Transform> transforms;
	private final int priority;
	private final Optional<Integer> phaseOverride;

	public TransformerDefinition(ClassTarget target, List<Transform> transforms, int priority, Optional<Integer> phaseOverride) {
		this.target = target;
		this.transforms = transforms;
		this.priority = priority;
		this.phaseOverride = phaseOverride;
	}

	public List<Transformer> decompose(Id baseId) {
		if (this.transforms.size() == 1) {
			Transform transform = this.transforms.get(0);
			int phase = this.phaseOverride.orElse(transform.type().defaultPhase);
			return Collections.singletonList(new Transformer(
					baseId, this.target, transform, this.priority, phase
			));
		}

		List<Transformer> transformers = new ArrayList<>();

		for (int i = 0; i < this.transforms.size(); i++) {
			Id id = baseId.suffixed("/" + i);
			Transform transform = this.transforms.get(i);
			int phase = this.phaseOverride.orElse(transform.type().defaultPhase);
			Transformer transformer = new Transformer(id, this.target, transform, this.priority, phase);
			transformers.add(transformer);
		}

		return transformers;
	}
}
