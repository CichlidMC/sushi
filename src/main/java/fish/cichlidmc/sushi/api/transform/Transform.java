package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.impl.registry.SushiBootstraps;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

/**
 * A transformation that can be applied to any class.
 */
public interface Transform {
	SimpleRegistry<MapCodec<? extends Transform>> REGISTRY = SimpleRegistry.create(SushiBootstraps::bootstrapTransforms);
	Codec<Transform> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), Transform::codec);

	/**
	 * Transform the given class.
	 * @throws TransformException if an error occurs during transformation
	 */
	void apply(TransformContext context) throws TransformException;

	MapCodec<? extends Transform> codec();
}
