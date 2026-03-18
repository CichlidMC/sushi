package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.PatternInstruction;
import fish.cichlidmc.sushi.api.model.code.element.pattern.NewObjectPatternInstruction;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.impl.model.code.element.InstructionHolderImpl;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;

public final class PatternInstructions {
	private PatternInstructions() {}

	public static void match(NavigableSet<InstructionHolder<?>> instructions) {
		Map<InstructionHolder<?>, PatternInstruction> replacements = new HashMap<>();

		outer: for (Iterator<InstructionHolder<?>> iterator = instructions.iterator(); iterator.hasNext();) {
			InstructionHolder<?> instruction = iterator.next();

			if (replacements.containsKey(instruction)) {
				// we'll get to you later
				continue;
			}

			if (instruction.get() instanceof NewObjectInstruction newInstruction) {
				// search for a corresponding <init> afterward
				int depth = 0;

				for (InstructionHolder<?> after : instruction.after()) {
					if (isEquivalentNew(newInstruction, after)) {
						// found an identical NEW instruction, increase depth
						depth++;
					} else if (after.get() instanceof InvokeInstruction invoke && isMatchingInit(newInstruction, invoke)) {
						// found a matching init, but this is only the right one if depth == 0
						if (depth == 0) {
							// this is it. remove the NEW, and mark the <init> for replacement.
							iterator.remove();
							replacements.put(after, NewObjectPatternInstruction.of(newInstruction, invoke));

							// the NEW should always be followed by a DUP, which we also need to remove
							if (!iterator.hasNext() || !(iterator.next().get() instanceof StackInstruction stack) || stack.opcode() != Opcode.DUP) {
								throw new IllegalStateException("NEW was not followed by a valid DUP");
							}

							iterator.remove();
							continue outer;
						} else {
							// this is not it, decrease depth
							depth--;
						}
					}
				}

				// we didn't find a match. there should always be one though, so something is wrong
				throw new IllegalStateException("Did not find a matching <init> for NEW: " + newInstruction);
			}
		}

		replacements.forEach((original, replacement) -> {
			instructions.remove(original);
			TransformableCode owner = original.owner();
			int index = original.index();
			InstructionHolder.Pattern<?> newHolder = new InstructionHolderImpl.PatternImpl<>(owner, index, replacement);
			instructions.add(newHolder);
		});
	}

	private static boolean isEquivalentNew(NewObjectInstruction first, InstructionHolder<?> after) {
		if (!(after.get() instanceof NewObjectInstruction second))
			return false;

		return first.className().equals(second.className());
	}

	private static boolean isMatchingInit(NewObjectInstruction newInstruction, InvokeInstruction invoke) {
		return Instructions.isConstructor(invoke) && invoke.owner().equals(newInstruction.className());
	}
}
