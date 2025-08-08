package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.impl.model.code.InstructionListImpl;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;

import java.util.List;

/**
 * List of {@link Instruction}s and {@link PseudoInstruction}s within a method body.
 * Provides a faster index lookup and comparison methods.
 * @see InstructionComparisons
 */
public sealed interface InstructionList extends InstructionComparisons permits InstructionListImpl {
	List<CodeElement> asList();

	/**
	 * Create a sub-list that only spans between the two given instructions.
	 * Whether each end is included or not depends on its offset.
	 * @throws IllegalArgumentException if either instruction is not contained in this list or if the end comes before the start
	 */
	InstructionList subList(Point from, Point to);
}
