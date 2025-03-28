package io.github.cichlidmc.sushi.impl.exp;

import io.github.cichlidmc.sushi.impl.util.MethodDescription;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InvokeExpressionTarget implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodDescription.WITH_CLASS_CODEC.xmap(
			InvokeExpressionTarget::new, invoke -> invoke.target
	).fieldOf("method");

	private final MethodDescription target;

	private InvokeExpressionTarget(MethodDescription target) {
		this.target = target;
	}

	@Override
	public Collection<AbstractInsnNode> find(InsnList instructions) {
		List<AbstractInsnNode> found = new ArrayList<>();
		for (AbstractInsnNode insn : instructions) {
			if (insn instanceof MethodInsnNode && this.target.matches((MethodInsnNode) insn)) {
				found.add(insn);
			}
		}
		return found;
	}

	@Override
	public MapCodec<? extends ExpressionTarget> codec() {
		return CODEC;
	}
}
