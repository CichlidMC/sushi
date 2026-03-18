package fish.cichlidmc.sushi.impl.model.code.element;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.PatternInstruction;

import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.util.NavigableSet;
import java.util.Optional;

public abstract sealed class InstructionHolderImpl<T> implements InstructionHolder<T> {
	private final TransformableCode owner;
	private final int index;
	private final T wrapped;

	protected InstructionHolderImpl(TransformableCode owner, int index, T wrapped) {
		this.owner = owner;
		this.index = index;
		this.wrapped = wrapped;
	}

	@Override
	public TransformableCode owner() {
		return this.owner;
	}

	@Override
	public Optional<InstructionHolder<?>> next() {
		return Optional.ofNullable(this.owner.instructions().higher(this));
	}

	@Override
	public Optional<InstructionHolder<?>> previous() {
		return Optional.ofNullable(this.owner.instructions().lower(this));
	}

	@Override
	public NavigableSet<InstructionHolder<?>> after() {
		return this.owner.instructions().tailSet(this, false);
	}

	@Override
	public NavigableSet<InstructionHolder<?>> before() {
		return this.owner.instructions().headSet(this, false);
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
	@SuppressWarnings("unchecked")
	public <I> InstructionHolder<I> checkHolding(Class<I> clazz) {
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
			super(owner, index, wrapped);
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

		@Override
		public <I extends PatternInstruction> Pattern<I> checkHoldingPattern(Class<I> clazz) throws ClassCastException {
			throw createClassCastException(Real.class, Pattern.class);
		}
	}

	public static final class PseudoImpl<T extends PseudoInstruction> extends InstructionHolderImpl<T> implements Pseudo<T> {
		public PseudoImpl(TransformableCode owner, int index, T wrapped) {
			super(owner, index, wrapped);
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

		@Override
		public <I extends PatternInstruction> Pattern<I> checkHoldingPattern(Class<I> clazz) throws ClassCastException {
			throw createClassCastException(Real.class, Pattern.class);
		}
	}

	public static final class PatternImpl<T extends PatternInstruction> extends InstructionHolderImpl<T> implements InstructionHolder.Pattern<T> {
		public PatternImpl(TransformableCode owner, int index, T wrapped) {
			super(owner, index, wrapped);
		}

		@Override
		public <I extends Instruction> Real<I> checkHoldingReal(Class<I> clazz) throws ClassCastException {
			throw createClassCastException(Pattern.class, Real.class);
		}

		@Override
		public <I extends PseudoInstruction> Pseudo<I> checkHoldingPseudo(Class<I> clazz) throws ClassCastException {
			throw createClassCastException(Pattern.class, Pseudo.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <I extends PatternInstruction> Pattern<I> checkHoldingPattern(Class<I> clazz) throws ClassCastException {
			this.checkHolding(clazz);
			return (Pattern<I>) this;
		}
	}
}
