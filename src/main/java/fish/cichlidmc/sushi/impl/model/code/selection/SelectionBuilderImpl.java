package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.impl.model.code.InstructionListImpl;
import org.glavo.classfile.CodeElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SelectionBuilderImpl implements Selection.Builder {
	public final List<SelectionImpl> selections;

	private final Supplier<Id> currentTransformer;
	private final InstructionListImpl instructions;

	public SelectionBuilderImpl(Supplier<Id> currentTransformer, InstructionListImpl instructions) {
		this.selections = new ArrayList<>();
		this.currentTransformer = currentTransformer;
		this.instructions = instructions;
	}

	@Override
	public Selection only(CodeElement instruction) {
		return this.newSelection(Point.before(instruction), Point.after(instruction));
	}

	@Override
	public Selection before(CodeElement instruction) {
		return this.at(Point.before(instruction));
	}

	@Override
	public Selection after(CodeElement instruction) {
		return this.at(Point.after(instruction));
	}

	@Override
	public Selection at(Point point) {
		return this.newSelection(point, point);
	}

	@Override
	public WithStart from(CodeElement instruction, boolean inclusive) {
		Point start = new Point(instruction, inclusive ? Point.Offset.BEFORE : Point.Offset.AFTER);
		return new WithStartImpl(start);
	}

	public void checkForConflicts() throws TransformException {
		for (SelectionImpl first : this.selections) {
			for (SelectionImpl second : this.selections) {
				if (first != second && first.conflictsWith(second)) {
					throw new TransformException("Conflicting selections made by [" + first.owner + "] and [" + second.owner + ']');
				}
			}
		}
	}

	private Selection newSelection(Point start, Point end) {
		Id owner = this.currentTransformer.get();
		SelectionImpl selection = new SelectionImpl(owner, start, end, this.instructions);
		this.selections.add(selection);
		return selection;
	}

	public final class WithStartImpl implements WithStart {
		private final Point start;

		public WithStartImpl(Point start) {
			this.start = start;
		}

		@Override
		public Selection to(CodeElement instruction, boolean inclusive) {
			Point end = new Point(instruction, inclusive ? Point.Offset.AFTER : Point.Offset.BEFORE);

			if (SelectionBuilderImpl.this.instructions.compare(this.start, end) > 0) {
				throw new IllegalArgumentException("Start point comes after end");
			}

			return SelectionBuilderImpl.this.newSelection(this.start, end);
		}
	}
}
