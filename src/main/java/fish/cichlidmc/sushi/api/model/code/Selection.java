package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.transform.wrap_op.Operation;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.MethodBuilder;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

/**
 * A Selection represents a range in a method's instructions where each end is anchored before/after an instruction.
 * Selections may be empty, where both ends are anchored to the same side of the same instruction.
 * <p>
 * Each selection may be used to perform multiple operations, which will be applied in order.
 */
public sealed interface Selection permits SelectionImpl {
	Point start();

	Point end();

	/**
	 * Insert a block of code either before or after this selection.
	 * <p>
	 * This is a very safe operation, and will never cause a hard conflict.
	 * Of course, that doesn't mean that it's foolproof; logical conflicts are very possible.
	 * <p>
	 * Beware that it is possible that other transforms insert their own arbitrary code within this Selection.
	 * Therefore, it is not safe to insert code both before and after, and assume that both blocks will run.
	 */
	void insert(CodeBlock code, Offset offset);

	/**
	 * Shortcut for {@link #insert(CodeBlock, Offset) insert(code, Offset.BEFORE)}.
	 */
	void insertBefore(CodeBlock code);

	/**
	 * Shortcut for {@link #insert(CodeBlock, Offset) insert(code, Offset.AFTER)}.
	 */
	void insertAfter(CodeBlock code);

	/**
	 * Replace all instructions within this selection with new ones.
	 * <p>
	 * <strong>This operation is dangerous.</strong>
	 * A replacement will hard conflict with any other overlapping changes.
	 */
	void replace(CodeBlock code);

	/**
	 * Split this selection off to a new method. Sushi will insert a {@link InvokeDynamicInstruction} that produces an
	 * {@link Operation} that, when invoked, will invoke the newly created method.
	 * <p>
	 * This operation is reasonably safe if used sparingly, and will only hard conflict with replacements and
	 * other extractions that partially intersect this one.
	 * <p>
	 * <strong>Sushi makes no attempt to ensure the validity of the resulting bytecode</strong>[1].
	 * Handle with care! The intended use case for this operation is the extraction of a single operation
	 * (the {@code wrap_operation} transform). Anything more is likely to break spectacularly.
	 * It is your job to ensure that no jumps cross the extraction boundaries and that the stack isn't mangled.
	 * <p>
	 * [1]: There is one exception: <strong>local variables</strong>. All references to local variables within
	 * the extracted code will be automatically fixed.
	 * @param init consumer to build the new method. Do not add code here; it will be overwritten.
	 * @param header {@link CodeBlock} to insert at the head of the new method
	 * @param footer {@link CodeBlock} to insert at the tail of the new method
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
		 * Begin a new selection starting at the given point.
		 */
		WithStart from(Point start);

		/**
		 * A half-build Selection with a start defined, but not an end.
		 */
		sealed interface WithStart permits SelectionBuilderImpl.WithStartImpl {
			/**
			 * Complete a selection by defining an end point.
			 * @throws IllegalArgumentException if the given instruction comes before the starting instruction
			 */
			Selection to(Point end);
		}
	}
}
