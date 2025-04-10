package io.github.cichlidmc.sushi.api.transform.expression;

import io.github.cichlidmc.sushi.api.util.JavaType;
import org.objectweb.asm.tree.AbstractInsnNode;

public final class FoundExpressionTarget {
	public final AbstractInsnNode instruction;
	public final JavaType type;

	public FoundExpressionTarget(AbstractInsnNode instruction, JavaType type) {
		this.instruction = instruction;
		this.type = type;
	}
}
