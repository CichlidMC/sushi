package fish.cichlidmc.sushi.impl.transformer.slice;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;

import java.util.NavigableSet;

public final class SlicedSelectionBuilder implements Selection.Builder {
	private final Selection.Builder wrapped;
	NavigableSet<InstructionHolder<?>> slicedInstructions;

	public SlicedSelectionBuilder(Selection.Builder wrapped, NavigableSet<InstructionHolder<?>> slicedInstructions) {
		this.wrapped = wrapped;
		this.slicedInstructions = slicedInstructions;
	}

	@Override
	public Selection only(InstructionHolder<?> instruction) {
		return this.wrapped.only(instruction);
	}

	@Override
	public Selection before(InstructionHolder<?> instruction) {
		return this.wrapped.before(instruction);
	}

	@Override
	public Selection after(InstructionHolder<?> instruction) {
		return this.wrapped.after(instruction);
	}

	@Override
	public Selection at(Point point) {
		return this.wrapped.at(point);
	}

	@Override
	public Selection head() {
		return this.wrapped.only(this.slicedInstructions.first());
	}

	@Override
	public Selection tail() {
		return this.wrapped.only(this.slicedInstructions.last());
	}

	@Override
	public WithStart from(Point start) {
		return this.wrapped.from(start);
	}
}
