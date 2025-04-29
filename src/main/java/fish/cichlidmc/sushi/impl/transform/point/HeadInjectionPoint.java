package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Collections;

public enum HeadInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<HeadInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<HeadInjectionPoint> MAP_CODEC = CODEC.fieldOf("unused");

	@Override
	public Collection<? extends AbstractInsnNode> find(InsnList instructions) {
		return Collections.singleton(instructions.getFirst());
	}

	@Override
	public String describe() {
		return "head";
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
