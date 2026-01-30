package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

public final class SelectionBuilderImpl implements Selection.Builder {
	public final List<SelectionImpl> selections;

	private final NavigableSet<InstructionHolder<?>> instructions;
	private final Operations operations;

	public SelectionBuilderImpl(NavigableSet<InstructionHolder<?>> instructions, Operations operations) {
		this.selections = new ArrayList<>();
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
		return this.only(this.instructions.getFirst());
	}

	@Override
	public Selection tail() {
		return this.only(this.instructions.getLast());
	}

	@Override
	public WithStart from(Point start) {
		return new WithStartImpl(start);
	}

	private Selection newSelection(Point start, Point end) {
		Id owner = TransformContextImpl.current().transformerId();
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
