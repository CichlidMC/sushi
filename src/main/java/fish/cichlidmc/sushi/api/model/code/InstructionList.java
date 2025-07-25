package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.impl.model.code.InstructionListImpl;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;

import java.util.List;

/**
 * List of {@link Instruction}s and {@link PseudoInstruction}s within a method body.
 * Provides a faster index lookup and comparison methods.
 */
public sealed interface InstructionList permits InstructionListImpl {
	List<CodeElement> asList();

	/**
	 * @throws IllegalArgumentException if the instruction is not contained in this list
	 */
	int indexOf(CodeElement instruction);

	/**
	 * Compare the two given instructions by index.
	 * @throws IllegalArgumentException if either instruction is not contained in this list
	 */
	int compare(CodeElement first, CodeElement second);

	/**
	 * Compare the two given points by index and offset.
	 * @throws IllegalArgumentException if either point's instruction is not contained in this list
	 */
	int compare(Point first, Point second);

	/**
	 * Create a sub-list that only spans between the two given instructions.
	 * Whether each end is included or not depends on its offset.
	 * @throws IllegalArgumentException if either instruction is not contained in this list or if the end comes before the start
	 */
	InstructionList subList(Point from, Point to);
}
