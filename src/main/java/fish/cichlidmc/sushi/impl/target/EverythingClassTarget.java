package fish.cichlidmc.sushi.impl.target;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.Optional;
import java.util.Set;

public enum EverythingClassTarget implements ClassTarget {
	INSTANCE;

	public static final MapCodec<EverythingClassTarget> CODEC = Codec.unit(INSTANCE).fieldOf("unused");

	@Override
	public boolean shouldApply(ClassNode target) {
		return true;
	}

	@Override
	public Optional<Set<String>> concreteTargets() {
		return Optional.empty();
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return CODEC;
	}
}
