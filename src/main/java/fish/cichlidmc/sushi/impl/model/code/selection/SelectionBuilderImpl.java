package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.operation.Operations;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.Supplier;

public final class SelectionBuilderImpl implements Selection.Builder {
	public final List<SelectionImpl> selections;

	private final Supplier<Id> currentTransformer;
	private final NavigableSet<InstructionHolder<?>> instructions;
	private final Operations operations;

	public SelectionBuilderImpl(Supplier<Id> currentTransformer, NavigableSet<InstructionHolder<?>> instructions, Operations operations) {
		this.selections = new ArrayList<>();
		this.currentTransformer = currentTransformer;
		this.instructions = instructions;
		this.operations = operations;
	}

	@Override
	public Selection only(InstructionHolder<?> instruction) {
		return this.newSelection(Point.before(instruction), Point.after(instruction));
	}

	@Override
	public Selection before(InstructionHolder<?> instruction) {
		return this.at(Point.before(instruction));
	}

	@Override
	public Selection after(InstructionHolder<?> instruction) {
		return this.at(Point.after(instruction));
	}

	@Override
	public Selection at(Point point) {
		return this.newSelection(point, point);
	}

	@Override
	public Selection head() {
		return this.only(this.instructions.first());
	}

	@Override
	public Selection tail() {
		return this.only(this.instructions.last());
	}

	@Override
	public WithStart from(Point start) {
		return new WithStartImpl(start);
	}

	private Selection newSelection(Point start, Point end) {
		Id owner = this.currentTransformer.get();
		SelectionImpl selection = new SelectionImpl(owner, start, end, this.operations);
		this.selections.add(selection);
		return selection;
	}

	public final class WithStartImpl implements WithStart {
		private final Point start;

		public WithStartImpl(Point start) {
			this.start = start;
		}

		@Override
		public Selection to(Point end) {
			if (this.start.compareTo(end) > 0) {
				throw new IllegalArgumentException("Start point comes after end");
			}

			return SelectionBuilderImpl.this.newSelection(this.start, end);
		}
	}
}
