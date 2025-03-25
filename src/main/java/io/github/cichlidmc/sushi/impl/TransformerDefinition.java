package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class TransformerDefinition {
	public static final Codec<TransformerDefinition> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.listOrSingle().fieldOf("target"), def -> def.target,
			Transform.CODEC.listOrSingle().fieldOf("transform"), def -> def.transform,
			Codecs.INT.optional(0).fieldOf("priority"), def -> def.priority,
			TransformerDefinition::new
	).asCodec();

	public final List<ClassTarget> target;
	public final List<Transform> transform;
	public final int priority;

	public TransformerDefinition(List<ClassTarget> target, List<Transform> transform, int priority) {
		this.target = target;
		this.transform = transform;
		this.priority = priority;
	}

	public boolean apply(ClassNode node) throws TransformException {
		for (ClassTarget target : this.target) {
			if (target.shouldApply(node)) {
				boolean transformed = false;
				for (Transform transform : this.transform) {
					transformed |= transform.apply(node);
				}
				return transformed;
			}
		}

		return false;
	}
}
