package io.github.cichlidmc.sushi.api.target;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.target.SingleClassTarget;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.Optional;
import java.util.Set;

/**
 * An arbitrary predicate for {@link ClassNode}s, determining if a transform should apply or not.
 */
public interface ClassTarget {
	SimpleRegistry<MapCodec<? extends ClassTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTargets);
	Codec<ClassTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ClassTarget::codec)
			.withAlternative(SingleClassTarget.CODEC) // single class is an alternative to allow inlining a class name directly
			.withAlternative(UnionClassTarget.CODEC); // and union is an alternative to allow inlining an array of targets

	boolean shouldApply(ClassNode target);

	/**
	 * If all possible class targets are known up-front, then Sushi can speed up Transformer querying.
	 * Return a set of all possible targets, if present, to opt in.
	 * @implNote {@link #shouldApply} will never be queried for a class outside of this set, but will still be queried for ones inside it.
	 */
	Optional<Set<String>> concreteTargets();

	MapCodec<? extends ClassTarget> codec();
}
