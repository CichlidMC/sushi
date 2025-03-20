package io.github.cichlidmc.sushi.api.target;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

/**
 * Represents a set of one or more class targets for a transformer.
 */
public interface ClassTarget {
	/**
	 * The registry of target types. Register new ones here with a unique ID.
	 */
	SimpleRegistry<MapCodec<? extends ClassTarget>> REGISTRY = SimpleRegistry.create();

	boolean shouldApply(ClassNode target);

	MapCodec<? extends ClassTarget> codec();
}
