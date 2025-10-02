package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.model.code.InstructionListImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import org.glavo.classfile.CodeElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SelectionBuilderImpl implements Selection.Builder {
	public final List<SelectionImpl> selections;

	private final Supplier<Id> currentTransformer;
	private final InstructionListImpl instructions;
	private final Operations operations;

	public SelectionBuilderImpl(Supplier<Id> currentTransformer, InstructionListImpl instructions, Operations operations) {
		this.selections = new ArrayList<>();
		this.currentTransformer = currentTransformer;
		this.instructions = instructions;
		this.operations = operations;
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
	public Selection head() {
		CodeElement first = this.instructions.asList().getFirst();
		return this.only(first);
	}

	@Override
	public Selection tail() {
		CodeElement last = this.instructions.asList().getLast();
		return this.only(last);
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
			if (SelectionBuilderImpl.this.instructions.compare(this.start, end) > 0) {
				throw new IllegalArgumentException("Start point comes after end");
			}

			return SelectionBuilderImpl.this.newSelection(this.start, end);
		}
	}
}
