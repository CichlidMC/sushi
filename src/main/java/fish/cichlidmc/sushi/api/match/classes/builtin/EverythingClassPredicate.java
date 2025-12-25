package fish.cichlidmc.sushi.api.match.classes.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/// A [ClassPredicate] matching **every single class.**
///
/// This is the nuclear option that should be avoided unless truly necessary.
public enum EverythingClassPredicate implements ClassPredicate {
	INSTANCE;

	public static final MapCodec<EverythingClassPredicate> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean shouldApply(ClassModel model) {
		return true;
	}

	@Override
	public Optional<Set<ClassDesc>> concreteMatches() {
		return Optional.empty();
	}

	@Override
	public MapCodec<? extends ClassPredicate> codec() {
		return CODEC;
	}
}
