package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

/**
 * Defines a set of transforms to apply to one or more classes.
 */
public interface Transformer {
	SimpleRegistry<MapCodec<? extends Transformer>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<Transformer> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), Transformer::codec);

	/**
	 * Register all transforms that this transformer wants to apply.
	 * @throws TransformException if something goes wrong during registration
	 */
	void register(Transforms transforms) throws TransformException;

	MapCodec<? extends Transformer> codec();
}
