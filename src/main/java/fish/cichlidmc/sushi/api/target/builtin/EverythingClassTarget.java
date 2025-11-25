package fish.cichlidmc.sushi.api.target.builtin;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.ClassModel;

import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ClassTarget} matching <strong>every single class.</strong>
 * <p>
 * This is the nuclear option that should be avoided unless truly necessary.
 */
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
