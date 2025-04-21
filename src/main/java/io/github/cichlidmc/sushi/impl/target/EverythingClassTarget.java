package io.github.cichlidmc.sushi.impl.target;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
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
