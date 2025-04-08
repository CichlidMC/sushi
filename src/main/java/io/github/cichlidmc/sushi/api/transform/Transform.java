package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.tinycodecs.Codec;
import org.objectweb.asm.tree.ClassNode;

/**
 * A transformation that can be applied to any class.
 * @see TransformType
 */
public interface Transform {
	Codec<Transform> CODEC = TransformType.REGISTRY.byIdCodec().dispatch(Transform::type, type -> type.codec);

	/**
	 * Transform the given class.
	 * @return true if a transformation was applied
	 * @throws TransformException if an error occurs during transformation
	 */
	boolean apply(ClassNode node) throws TransformException;

	TransformType type();
}
