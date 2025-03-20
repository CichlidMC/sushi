package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.impl.target.ClassArrayTarget;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.MapCodec;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Function;

public final class TransformerInstance {
	// use ClassArrayTarget as an alternative to allow just inlining a class name
	private static final Codec<ClassTarget> classArrayTargetCodec = ClassArrayTarget.CODEC.xmap(
			// Function.identity() does not work here because generics
			array -> array,
			target -> {
				// withAlternative always encodes with the first codec, so just throw here
				throw new RuntimeException("Cannot convert ClassArrayTarget to ClassTarget. This shouldn't even be called!");
			}
	);

	private static final Codec<ClassTarget> classTargetCodec = ClassTarget.REGISTRY.byIdCodec().dispatch(
			ClassTarget::codec, Function.identity()
	).withAlternative(classArrayTargetCodec);

	private static final Codec<Transform> transformCodec = Transform.REGISTRY.byIdCodec().dispatch(
			Transform::codec, Function.identity()
	);

	public static final MapCodec<TransformerInstance> MAP_CODEC = CompositeCodec.of(
			classTargetCodec.fieldOf("target"), transformer -> transformer.target,
			transformCodec.fieldOf("transform"), transformer -> transformer.transform,
			TransformerInstance::new
	);
	public static final Codec<TransformerInstance> CODEC = MAP_CODEC.asCodec();

	public final ClassTarget target;
	public final Transform transform;

	public TransformerInstance(ClassTarget target, Transform transform) {
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
