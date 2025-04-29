package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.SushiMetadata;
import fish.cichlidmc.tinycodecs.Codec;

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
	boolean apply(TransformContext context) throws TransformException;

	/**
	 * Describe the transformation applied by this transform in a single-line, human-readable string.
	 * This is used for documenting the changes made by transformers through {@link SushiMetadata}.
	 */
	String describe();

	TransformType type();
}
