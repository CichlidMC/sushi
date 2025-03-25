package io.github.cichlidmc.sushi.impl.point;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Collections;

public enum TailInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<TailInjectionPoint> CODEC = Codecs.unit(INSTANCE);
	public static final MapCodec<TailInjectionPoint> MAP_CODEC = CODEC.fieldOf("unused");

	@Override
	public Collection<AbstractInsnNode> find(InsnList instructions) {
		return Collections.singletonList(instructions.getLast());
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
