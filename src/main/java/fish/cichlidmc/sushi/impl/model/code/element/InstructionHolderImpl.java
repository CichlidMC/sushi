package fish.cichlidmc.sushi.impl.model.code.element;

import fish.cichlidmc.fishflakes.api.Either;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;

import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.util.function.Function;

public abstract sealed class InstructionHolderImpl<T extends CodeElement> implements InstructionHolder<T> {
	private final TransformableCode owner;
	private final int index;
	private final T wrapped;
	private final Either<Instruction, PseudoInstruction> either;

	protected InstructionHolderImpl(TransformableCode owner, int index, T wrapped, Function<T, Either<Instruction, PseudoInstruction>> eitherFunction) {
		this.owner = owner;
		this.index = index;
		this.wrapped = wrapped;
		this.either = eitherFunction.apply(wrapped);
	}

	@Override
	public TransformableCode owner() {
		return this.owner;
	}

	@Override
	public int index() {
		return this.index;
	}

	@Override
	public T get() {
		return this.wrapped;
	}

	@Override
	public Either<Instruction, PseudoInstruction> asEither() {
		return this.either;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends CodeElement> InstructionHolder<I> checkHolding(Class<I> clazz) {
		if (!clazz.isInstance(this.wrapped)) {
			throw createClassCastException(this.wrapped.getClass(), clazz);
		}

		return (InstructionHolder<I>) this;
	}

	@Override
	public int compareTo(InstructionHolder<?> that) {
		return Integer.compare(this.index(), that.index());
	}

	@Override
	public int hashCode() {
		return this.index;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return this.wrapped.toString();
	}

	private static ClassCastException createClassCastException(Class<?> from, Class<?> to) {
		return new ClassCastException("Class " + from + " cannot be cast to " + to);
	}

	public static final class RealImpl<T extends Instruction> extends InstructionHolderImpl<T> implements Real<T> {
		public RealImpl(TransformableCode owner, int index, T wrapped) {
			super(owner, index, wrapped, Either::left);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <I extends Instruction> Real<I> checkHoldingReal(Class<I> clazz) {
			this.checkHolding(clazz);
			return (Real<I>) this;
		}

		@Override
		public <I extends PseudoInstruction> Pseudo<I> checkHoldingPseudo(Class<I> clazz) {
			throw createClassCastException(Real.class, Pseudo.class);
		}
	}

	public static final class PseudoImpl<T extends PseudoInstruction> extends InstructionHolderImpl<T> implements Pseudo<T> {
		public PseudoImpl(TransformableCode owner, int index, T wrapped) {
			super(owner, index, wrapped, Either::right);
		}

		@Override
		public <I extends Instruction> Real<I> checkHoldingReal(Class<I> clazz) {
			throw createClassCastException(Pseudo.class, Real.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <I extends PseudoInstruction> Pseudo<I> checkHoldingPseudo(Class<I> clazz) {
			this.checkHolding(clazz);
			return (Pseudo<I>) this;
		}
	}
}
