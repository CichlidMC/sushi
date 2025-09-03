package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.SushiMetadata;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

/**
 * A transformation that can be applied to any class.
 */
public interface Transform {
	SimpleRegistry<MapCodec<? extends Transform>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTransforms);
	Codec<Transform> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), Transform::codec);

	/**
	 * Transform the given class.
	 * @throws TransformException if an error occurs during transformation
	 */
	void apply(TransformContext context) throws TransformException;

	/**
	 * Describe the transformation applied by this transform in a single-line, human-readable string.
	 * This is used for documenting the changes made by transformers through {@link SushiMetadata}.
	 */
	String describe();

	MapCodec<? extends Transform> codec();
}
