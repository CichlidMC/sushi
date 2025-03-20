package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

/**
 * A transformation that can be applied to any class.
 */
public interface Transform {
	/**
	 * The registry of transform types. Register new ones here with a unique ID.
	 */
	SimpleRegistry<MapCodec<? extends Transform>> REGISTRY = SimpleRegistry.create();

	/**
	 * Transform the given class.
	 * @return true if a transformation was applied
	 * @throws TransformException if an error occurs during transformation
	 */
	boolean apply(ClassNode node) throws TransformException;

	MapCodec<? extends Transform> codec();
}
