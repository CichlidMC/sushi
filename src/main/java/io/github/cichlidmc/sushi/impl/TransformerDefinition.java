package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.Transformer;
import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A parsed transformer JSON, before being split.
 */
public final class TransformerDefinition {
	public static final Codec<TransformerDefinition> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), def -> def.target,
			Transform.CODEC.listOrSingle().fieldOf("transform"), def -> def.transforms,
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
