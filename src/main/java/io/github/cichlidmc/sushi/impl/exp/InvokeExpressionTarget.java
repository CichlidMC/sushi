package io.github.cichlidmc.sushi.impl.exp;

import io.github.cichlidmc.sushi.impl.util.MethodDesc;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Collections;

public final class InvokeExpressionTarget implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodDesc.CODEC.xmap(
			InvokeExpressionTarget::new, invoke -> invoke.target
	).fieldOf("method");

	private final MethodDesc target;

	private InvokeExpressionTarget(MethodDesc target) {
		this.target = target;
	}

	@Override
	public Collection<AbstractInsnNode> find(InsnList instructions) {
		return Collections.emptyList();
	}
}
