package fish.cichlidmc.sushi.api.target.builtin;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/// A compound [ClassTarget] which matches any class targeted by any of its components.
public record UnionClassTarget(List<ClassTarget> targets) implements ClassTarget {
	// this needs to be lazy because of the circular reference between it and ClassTarget.CODEC
	public static final Codec<UnionClassTarget> CODEC = Codec.lazy(
			() -> ClassTarget.CODEC.listOf().xmap(UnionClassTarget::new, UnionClassTarget::targets)
	);
	public static final MapCodec<UnionClassTarget> MAP_CODEC = CODEC.fieldOf("targets");

	public UnionClassTarget(ClassTarget... targets) {
		this(List.of(targets));
	}

	@Override
	public boolean shouldApply(ClassModel target) {
		for (ClassTarget entry : this.targets) {
			if (entry.shouldApply(target)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Optional<Set<ClassDesc>> concreteTargets() {
		Set<ClassDesc> targets = new HashSet<>();

		for (ClassTarget entry : this.targets) {
			Optional<Set<ClassDesc>> concrete = entry.concreteTargets();
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
