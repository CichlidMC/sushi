package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.tree.ClassNode;

public final class TargetedTransform {
	public static final Codec<TargetedTransform> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), transformer -> transformer.target,
			Transform.CODEC.fieldOf("transform"), transformer -> transformer.transform,
			TargetedTransform::new
	).asCodec();

	public final ClassTarget target;
	public final Transform transform;

	public TargetedTransform(ClassTarget target, Transform transform) {
		this.target = target;
		this.transform = transform;
	}

	public boolean apply(ClassNode node) throws TransformException {
		if (this.target.shouldApply(node)) {
			return this.transform.apply(node);
		}

		return false;
	}
}
