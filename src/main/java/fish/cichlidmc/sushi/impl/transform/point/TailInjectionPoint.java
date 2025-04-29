package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Collections;

public enum TailInjectionPoint implements InjectionPoint {
	INSTANCE;

	public static final Codec<TailInjectionPoint> CODEC = Codec.unit(INSTANCE);
	public static final MapCodec<TailInjectionPoint> MAP_CODEC = CODEC.fieldOf("unused");

	@Override
	public Collection<? extends AbstractInsnNode> find(InsnList instructions) {
		// walk backwards from end until a return is found
		for (AbstractInsnNode node = instructions.getLast(); node != null; node = node.getPrevious()) {
			if (ReturnInjectionPoint.isReturn(node.getOpcode())) {
				return Collections.singleton(node);
			}
		}

		return Collections.emptyList();
	}

	@Override
	public String describe() {
		return "tail";
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return MAP_CODEC;
	}
}
