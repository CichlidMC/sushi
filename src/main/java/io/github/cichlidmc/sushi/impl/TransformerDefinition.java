package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.tree.ClassNode;

public final class TransformerDefinition {
	public static final Codec<TransformerDefinition> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), def -> def.target,
			Transform.CODEC.fieldOf("transform"), def -> def.transform,
			Codecs.INT.optional(0).fieldOf("priority"), def -> def.priority,
			TransformerDefinition::new
	).asCodec();

	public final ClassTarget target;
	public final Transform transform;
	public final int priority;

	public TransformerDefinition(ClassTarget target, Transform transform, int priority) {
		this.target = target;
		this.transform = transform;
		this.priority = priority;
	}

	public boolean apply(ClassNode node) throws TransformException {
		if (this.target.shouldApply(node)) {
			return this.transform.apply(node);
		}

		return false;
	}
}
