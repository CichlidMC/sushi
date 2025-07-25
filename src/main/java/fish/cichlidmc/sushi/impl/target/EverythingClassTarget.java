package fish.cichlidmc.sushi.impl.target;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

public enum EverythingClassTarget implements ClassTarget {
	INSTANCE;

	public static final MapCodec<EverythingClassTarget> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean shouldApply(ClassModel target) {
		return true;
	}

	@Override
	public Optional<Set<ClassDesc>> concreteTargets() {
		return Optional.empty();
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return CODEC;
	}
}
