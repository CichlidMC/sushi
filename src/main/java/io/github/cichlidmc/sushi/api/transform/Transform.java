package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Function;

/**
 * A transformation that can be applied to any class.
 */
public interface Transform {
	SimpleRegistry<MapCodec<? extends Transform>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTransforms);
	Codec<Transform> CODEC = REGISTRY.byIdCodec().dispatch(Transform::codec, Function.identity());

	/**
	 * Transform the given class.
	 * @return true if a transformation was applied
	 * @throws TransformException if an error occurs during transformation
	 */
	boolean apply(ClassNode node) throws TransformException;

	MapCodec<? extends Transform> codec();
}
