package fish.cichlidmc.sushi.api.model.code.element;

import fish.cichlidmc.fishflakes.api.Either;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.code.element.InstructionHolderImpl;

import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.util.NavigableSet;
import java.util.Optional;

/// A location-aware wrapper around either an [Instruction] or a [PseudoInstruction].
/// @param <T> the type of the held instruction. Bound to [CodeElement] even though it will always
///            be an [Instruction] or [PseudoInstruction], since that's the best common type.
public sealed interface InstructionHolder<T extends CodeElement> extends Comparable<InstructionHolder<?>>
		permits InstructionHolder.Real, InstructionHolder.Pseudo, InstructionHolderImpl {

	/// @return the [TransformableCode] this instruction belongs to
	TransformableCode owner();

	/// @return the [InstructionHolder] with the next index, or empty if this is the last one
	Optional<InstructionHolder<?>> next();

	/// @return the [InstructionHolder] with the previous index, or empty if this is the first one
	Optional<InstructionHolder<?>> previous();

	/// @return the possibly empty set of instructions that come after this one
	NavigableSet<InstructionHolder<?>> after();

	/// @return the possibly empty set of instructions that come before this one
	NavigableSet<InstructionHolder<?>> before();

	/// @return the index of this instruction in the bytecode of its containing method
	int index();

	/// @return the held instruction
	T get();

	/// @return an [Either], holding either the held [Instruction] or [PseudoInstruction]
	Either<Instruction, PseudoInstruction> asEither();

	/// Perform a runtime-checked cast to a holder holding the given type of instruction.
	/// This is useful when a type is known, and the compiler just needs to be convinced. For example:
	/// ```java
	/// InstructionHolder<?> instruction;
	/// List<InstructionHolder.Real<InvokeInstruction> invokes;
	///
	/// if (instruction.get() instanceof InvokeInstruction invoke) {
	/// 	// we now know that T is bound to InvokeInstruction, and since
	/// 	// InvokeInstruction extends Instruction, then 'instruction'
	/// 	// must also be an InstructionHolder.Real.
	/// 	invokes.add(instruction.checkHoldingReal(InvokeInstruction.class));
	/// }
 	/// ```
	/// @return this
	/// @throws ClassCastException if the cast is invalid
	<I extends CodeElement> InstructionHolder<I> checkHolding(Class<I> clazz) throws ClassCastException;

	/// An extension of [#checkHolding(java.lang.Class)], that also casts to [Real].
	<I extends Instruction> InstructionHolder.Real<I> checkHoldingReal(Class<I> clazz) throws ClassCastException;

	/// An extension of [#checkHolding(java.lang.Class)], that also casts to [Pseudo].
	<I extends PseudoInstruction> InstructionHolder.Pseudo<I> checkHoldingPseudo(Class<I> clazz) throws ClassCastException;

	sealed interface Real<T extends Instruction> extends InstructionHolder<T> permits InstructionHolderImpl.RealImpl {
	}

	sealed interface Pseudo<T extends PseudoInstruction> extends InstructionHolder<T> permits InstructionHolderImpl.PseudoImpl {
	}
}
