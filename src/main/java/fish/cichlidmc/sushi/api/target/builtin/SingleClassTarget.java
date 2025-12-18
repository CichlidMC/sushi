package fish.cichlidmc.sushi.api.target.builtin;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/// A [ClassTarget] matching a single class.
public record SingleClassTarget(ClassDesc target) implements ClassTarget {
	public static final Codec<SingleClassTarget> CODEC = ClassDescs.CLASS_CODEC.xmap(
			SingleClassTarget::new, SingleClassTarget::target
	);
	public static final MapCodec<SingleClassTarget> MAP_CODEC = CODEC.fieldOf("class");

	@Override
	public boolean shouldApply(ClassModel target) {
		return this.target.equals(target.thisClass().asSymbol());
	}

	@Override
	public Optional<Set<ClassDesc>> concreteTargets() {
		return Optional.of(Set.of(this.target));
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return MAP_CODEC;
	}
}
