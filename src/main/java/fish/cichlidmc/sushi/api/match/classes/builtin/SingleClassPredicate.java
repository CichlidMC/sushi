package fish.cichlidmc.sushi.api.match.classes.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/// A [ClassPredicate] matching a single class.
public record SingleClassPredicate(ClassDesc desc) implements ClassPredicate {
	public static final Codec<SingleClassPredicate> CODEC = ClassDescs.CLASS_CODEC.xmap(
			SingleClassPredicate::new, SingleClassPredicate::desc
	);
	public static final MapCodec<SingleClassPredicate> MAP_CODEC = CODEC.fieldOf("class");

	@Override
	public boolean shouldApply(ClassModel model) {
		return this.desc.equals(model.thisClass().asSymbol());
	}

	@Override
	public Optional<Set<ClassDesc>> concreteMatches() {
		return Optional.of(Set.of(this.desc));
	}

	@Override
	public MapCodec<? extends ClassPredicate> codec() {
		return MAP_CODEC;
	}
}
