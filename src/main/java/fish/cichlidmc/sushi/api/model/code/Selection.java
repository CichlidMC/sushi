package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

/**
 * A Selection represents a range in a method's instructions where each end is anchored before/after an instruction.
 * Selections may be empty, where both ends are anchored to the same side of the same instruction.
 * <p>
 * Each selection may be used to perform multiple operations, but they will be applied in order.
 * This means that a replacement will disregard all previous insertions.
 */
public sealed interface Selection permits SelectionImpl {
	Point start();

	Point end();

	/**
	 * Insert new code before this selection.
	 */
	void insertBefore(CodeBlock code);

	/**
	 * Insert new code after this selection.
	 */
	void insertAfter(CodeBlock code);

	/**
	 * Replace all instructions within this selection with new ones.
	 * This will discard all previous insertions, but new ones can be added after.
	 * <p>
	 * <strong>This operation is dangerous.</strong>
	 * It conflicts with all other overlapping selections.
	 * If any are present, an error will be thrown when transformation is attempted.
	 */
	void replace(CodeBlock code);

	/**
	 * Split this selection off to a new method. This is a stackable operation, as long as
	 * all other selections are either entirely within this one or entirely outside of it.
	 * @param init consumer to modify the new method. Do not add code here.
	 * @param header {@link CodeBlock} to insert at the head of the method
	 * @param footer {@link CodeBlock} to insert at the tail of the method
	 * @param replacement {@link CodeBlock} to replace the code that was relocated
	 */
	void extract(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> init, CodeBlock header, CodeBlock footer, CodeBlock replacement);

	sealed interface Builder permits SelectionBuilderImpl {
		/**
		 * Create a selection including just one instruction.
		 */
		Selection only(CodeElement instruction);

		/**
		 * Create an empty selection right before the given instruction.
		 */
		Selection before(CodeElement instruction);

		/**
		 * Create an empty selection right after the given instruction.
		 */
		Selection after(CodeElement instruction);

		/**
		 * Create an empty selection which both starts and ends at the given point.
		 */
		Selection at(Point point);

		/**
		 * Begin a new selection starting at the given instruction.
		 * @param inclusive whether the given instruction is included in the selection or not
		 */
		WithStart from(CodeElement instruction, boolean inclusive);

		/**
		 * A half-build Selection with a start defined, but not an end.
		 */
		sealed interface WithStart permits SelectionBuilderImpl.WithStartImpl {
			/**
			 * Complete a selection by defining an end point.
			 * @param inclusive whether the given instruction is included in the selection or not
			 * @throws IllegalArgumentException if the given instruction comes before the starting instruction
			 */
			Selection to(CodeElement instruction, boolean inclusive);
		}
	}
}
