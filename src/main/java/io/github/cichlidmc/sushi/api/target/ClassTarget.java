package io.github.cichlidmc.sushi.api.target;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.target.SingleClassTarget;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

/**
 * Represents a set of one or more class targets for a transformer.
 */
public interface ClassTarget {
	SimpleRegistry<MapCodec<? extends ClassTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTargets);
	Codec<ClassTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ClassTarget::codec)
			.withAlternative(SingleClassTarget.CODEC); // single class is an alternative to allow inlining a class name directly

	boolean shouldApply(ClassNode target);

	MapCodec<? extends ClassTarget> codec();
}
