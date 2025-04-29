package fish.cichlidmc.sushi.api.transform.expression;

import fish.cichlidmc.sushi.api.util.JavaType;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Collection;

public final class FoundExpressionTargets {
	public final Collection<? extends AbstractInsnNode> instructions;
	/**
	 * The JavaTypes on the stack acting as inputs to this instruction, ex. method parameters
	 */
	public final JavaType[] inputs;
	/**
	 * The JavaType produced by these instructions, ex. a method return type
	 */
	public final JavaType output;

	public FoundExpressionTargets(Collection<? extends AbstractInsnNode> instructions, JavaType[] inputs, JavaType output) {
		if (instructions.isEmpty()) {
			throw new IllegalArgumentException("No instructions are present");
		}

		this.instructions = instructions;
		this.inputs = inputs;
		this.output = output;
	}
}
