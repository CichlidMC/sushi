package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;

/**
 * Utilities for handling {@link Instruction}s and {@link PseudoInstruction}s.
 */
public final class Instructions {
	private Instructions() {}

	public static void assertInstruction(CodeElement element) {
		if (!isInstruction(element)) {
			throw new IllegalArgumentException("Not an Instruction or PseudoInstruction: " + element);
		}
	}

	public static boolean isInstruction(CodeElement element) {
		return element instanceof Instruction || element instanceof PseudoInstruction;
	}
}
