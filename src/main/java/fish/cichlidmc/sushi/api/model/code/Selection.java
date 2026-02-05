package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import fish.cichlidmc.sushi.impl.transformer.slice.SlicedSelectionBuilder;

/// A Selection represents a range in a method's instructions where each end is anchored before/after an instruction.
/// Selections may be empty, where both ends are anchored to the same side of the same instruction.
///
/// Each selection may be used to perform multiple operations, which will be applied in order.
///
/// Selections may also have a [Timing], which determines roughly when their operations will be applied.
/// For better control over the order of operations, see [Phase]s.
public sealed interface Selection permits SelectionImpl {
	Point start();

	Point end();

	/// @return the [Timing] of operations made with this selection
	Timing timing();

	/// @return true if the given point is within this selection
	/// @param startInclusive if true, then the point is considered contained if it's the start point
	/// @param endInclusive if true, then the point is considered contained if it's the end point
	boolean contains(Point point, boolean startInclusive, boolean endInclusive);

	/// @return true if the given instruction is within this selection
	boolean contains(InstructionHolder<?> instruction);

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
	/// **Sushi makes (nearly) no promises about the validity of the resulting bytecode**.
	/// Handle this with care! It's extremely easy to break things in a way that will be extremely hard to debug.
	///
	/// The intended use case for this operation is [the extraction of a single operation][WrapOpTransformer].
	/// Anything more is likely to break spectacularly.
	///
	/// There is one exception where Sushi will help you: **local variables**.
	///
	/// If Sushi detects that a local variable crosses the **start** of the extraction, then it will automatically
	/// create the proper infrastructure needed for it to be used seamlessly, both when loading and storing it.
	///
	/// @param name the full name of the lambda method to generate
	/// @param delta a [StackDelta.MethodLike] describing the changes this selection makes to the stack
	/// @param block a [CodeBlock] that will be invoked to replace the extracted operation.
	/// 			 When the block is invoked, the top of the stack will be as expected, but there will be a
	/// 			 new entry at the top. This entry will be an [Operation] which, when invoked, will invoke
	///				 the extracted lambda. The operation expects to be invoked with the same values that make
	/// 			 up the "inputs" of the `desc`, and will return the "output."
	void extract(String name, StackDelta.MethodLike delta, CodeBlock block);

	/// @return a new [Selection] covering the same range, but with the given [Timing]
	Selection timed(Timing timing);

	enum Timing {
		EARLY, DEFAULT, LATE;

		public boolean comesAfter(Timing other) {
			return this.ordinal() > other.ordinal();
		}
	}

	sealed interface Builder permits SelectionBuilderImpl, SlicedSelectionBuilder {
		/// Create a selection including just one instruction.
		Selection only(InstructionHolder<?> instruction);

		/// Create an empty selection right before the given instruction.
		Selection before(InstructionHolder<?> instruction);

		/// Create an empty selection right after the given instruction.
		Selection after(InstructionHolder<?> instruction);

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
