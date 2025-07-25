package fish.cichlidmc.sushi.api.target;

import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.sushi.impl.target.SingleClassTarget;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/**
 * An arbitrary predicate for {@link ClassModel}s, determining if a transform should apply or not.
 */
public interface ClassTarget {
	SimpleRegistry<MapCodec<? extends ClassTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTargets);
	Codec<ClassTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ClassTarget::codec)
			.withAlternative(SingleClassTarget.CODEC) // single class is an alternative to allow inlining a class name directly
			.withAlternative(UnionClassTarget.CODEC); // and union is an alternative to allow inlining an array of targets

	boolean shouldApply(ClassModel target);

	/**
	 * If all possible class targets are known up-front, then Sushi can speed up Transformer querying.
	 * Return a set of all possible targets, if present, to opt in.
	 * @implNote {@link #shouldApply} will never be queried for a class outside of this set, but will still be queried for ones inside it.
	 */
	Optional<Set<ClassDesc>> concreteTargets();

	MapCodec<? extends ClassTarget> codec();
}
