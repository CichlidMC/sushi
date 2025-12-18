package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import org.glavo.classfile.CodeElement;

import java.lang.constant.MethodTypeDesc;

/// A Selection represents a range in a method's instructions where each end is anchored before/after an instruction.
/// Selections may be empty, where both ends are anchored to the same side of the same instruction.
///
/// Each selection may be used to perform multiple operations, which will be applied in order.
public sealed interface Selection permits SelectionImpl {
	Point start();

	Point end();

	/// Insert a block of code either before or after this selection.
	///
	/// This is a very safe operation, and will never cause a hard conflict.
	/// Of course, that doesn't mean that it's foolproof; logical conflicts are very possible.
	///
	/// Beware that it is possible that other transforms insert their own arbitrary code within this Selection.
	/// Therefore, it is not safe to insert code both before and after, and assume that both blocks will run.
	void insert(CodeBlock code, Offset offset);

	/// Shortcut for [insert(code, Offset.BEFORE)][#insert(CodeBlock, Offset)].
	void insertBefore(CodeBlock code);

	/// Shortcut for [insert(code, Offset.AFTER)][#insert(CodeBlock, Offset)].
	void insertAfter(CodeBlock code);

	/// Replace all instructions within this selection with new ones.
	///
	/// **This operation is dangerous.**
	/// A replacement will hard conflict with any other overlapping changes.
	void replace(CodeBlock code);

	/// Split this selection off to a new lambda method.
	///
	/// This operation is reasonably safe if used with small scopes, and will only hard conflict with replacements and
	/// other extractions that partially intersect this one.
	///
	/// **Sushi makes no attempt to ensure the validity of the resulting bytecode**[1].
	/// Handle with care! The intended use case for this operation is the extraction of a single operation
	/// (the `wrap_operation` transform). Anything more is likely to break spectacularly.
	/// It is your job to ensure that no jumps cross the extraction boundaries and that the stack isn't mangled.
	///
	/// [1]: There is one exception: **local variables**. All references to local variables within
	/// the extracted code will be automatically passed along to the lambda.
	/// @param name the full name of the lambda method to generate
	/// @param desc a descriptor describing the "inputs" and "output" of the selected block of code.
	///                The "inputs", or parameters, would be the types on the top of the stack which are consumed during execution.
	///                The "output", or return type, would be the type left on the top of the stack after execution.
	///                For example, for just a static method invocation, this would just be its descriptor.
	/// @param block an [ExtractionCodeBlock] that will write the lambda invocation, and possibly additional code
	void extract(String name, MethodTypeDesc desc, ExtractionCodeBlock block);

	sealed interface Builder permits SelectionBuilderImpl {
		/// Create a selection including just one instruction.
		Selection only(CodeElement instruction);

		/// Create an empty selection right before the given instruction.
		Selection before(CodeElement instruction);

		/// Create an empty selection right after the given instruction.
		Selection after(CodeElement instruction);

		/// Create an empty selection which both starts and ends at the given point.
		Selection at(Point point);

		/// Create a selection containing only the first instruction.
		Selection head();

		/// Create a selection containing only the last instruction.
		Selection tail();

		/// Begin a new selection starting at the given point.
		WithStart from(Point start);

		/// A half-build Selection with a start defined, but not an end.
		sealed interface WithStart permits SelectionBuilderImpl.WithStartImpl {
			/// Complete a selection by defining an end point.
			/// @throws IllegalArgumentException if the given instruction comes before the starting instruction
			Selection to(Point end);
		}
	}
}
