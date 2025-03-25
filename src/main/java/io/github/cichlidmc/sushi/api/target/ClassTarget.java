package io.github.cichlidmc.sushi.api.target;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.target.SingleClassTarget;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Function;

/**
 * Represents a set of one or more class targets for a transformer.
 */
public interface ClassTarget {
	SimpleRegistry<MapCodec<? extends ClassTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTargets);
	Codec<ClassTarget> CODEC = REGISTRY.byIdCodec().dispatch(ClassTarget::codec, Function.identity())
			.withAlternative(SingleClassTarget.CODEC); // single class is an alternative to allow inlining a class name directly

	boolean shouldApply(ClassNode target);

	MapCodec<? extends ClassTarget> codec();
}
