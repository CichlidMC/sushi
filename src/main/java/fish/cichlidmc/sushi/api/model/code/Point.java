package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.util.Instructions;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;

/**
 * A point somewhere in a method's instructions.
 * Points are anchored either before or after a single instruction.
 * @param instruction either a {@link Instruction} or {@link PseudoInstruction} within a method body
 */
public record Point(CodeElement instruction, Offset offset) {
	/**
	 * @throws IllegalArgumentException if the given instruction is not actually an {@link Instruction} or {@link PseudoInstruction}
	 */
	public Point {
		Instructions.assertInstruction(instruction);
	}

	public static Point before(CodeElement instruction) {
		return new Point(instruction, Offset.BEFORE);
	}

	public static Point after(CodeElement instruction) {
		return new Point(instruction, Offset.AFTER);
	}
}
