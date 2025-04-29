package fish.cichlidmc.sushi.api.target;

import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A wrapper around many class targets, matching classes matched by any wrapped target.
 */
public class UnionClassTarget implements ClassTarget {
	// this needs to be lazy because of the circular reference between it and ClassTarget.CODEC
	public static final Codec<UnionClassTarget> CODEC = Codec.lazy(
			() -> ClassTarget.CODEC.listOf().xmap(UnionClassTarget::new, union -> union.list)
	);
	public static final MapCodec<UnionClassTarget> MAP_CODEC = CODEC.fieldOf("targets");

	private final List<ClassTarget> list;

	public UnionClassTarget(List<ClassTarget> list) {
		this.list = list;
	}

	@Override
	public boolean shouldApply(ClassNode target) {
		for (ClassTarget entry : this.list) {
			if (entry.shouldApply(target)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Optional<Set<String>> concreteTargets() {
		Set<String> targets = new HashSet<>();

		for (ClassTarget entry : this.list) {
			Optional<Set<String>> concrete = entry.concreteTargets();
			if (concrete.isPresent()) {
				targets.addAll(concrete.get());
			} else {
				// if any entry has non-concrete targets, then the slow path is necessary
				return Optional.empty();
			}
		}

		return Optional.of(targets);
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return MAP_CODEC;
	}
}
