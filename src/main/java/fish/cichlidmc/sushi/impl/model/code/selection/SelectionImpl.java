package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.operation.Extraction;
import fish.cichlidmc.sushi.impl.operation.Insertion;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.operation.Replacement;

public final class SelectionImpl implements Selection {
	public final Id owner;

	private final Point start;
	private final Point end;
	private final Timing timing;
	private final Operations operations;

	public SelectionImpl(Id owner, Point start, Point end, Timing timing, Operations operations) {
		this.owner = owner;
		this.start = start;
		this.end = end;
		this.timing = timing;
		this.operations = operations;
	}

	public SelectionImpl(Id owner, Point start, Point end, Operations operations) {
		this(owner, start, end, Timing.DEFAULT, operations);
	}

	@Override
	public Point start() {
		return this.start;
	}

	@Override
	public Point end() {
		return this.end;
	}

	@Override
	public Timing timing() {
		return this.timing;
	}

	@Override
	public void insert(CodeBlock code, Offset offset) {
		Point point = switch (offset) {
			case BEFORE -> this.start;
			case AFTER -> this.end;
		};

		this.operations.add(new Insertion(point, code, this.owner, this.timing));
	}

	@Override
	public void insertBefore(CodeBlock code) {
		this.insert(code, Offset.BEFORE);
	}

	@Override
	public void insertAfter(CodeBlock code) {
		this.insert(code, Offset.AFTER);
	}

	@Override
	public void replace(CodeBlock code) {
		this.operations.add(new Replacement(this.start, this.end, code, this.owner));
	}

	@Override
	public void extract(String name, StackDelta.MethodLike delta, CodeBlock block) {
		this.operations.add(new Extraction(this.start, this.end, name, delta, block, this.owner, this.timing));
	}

	@Override
	public Selection timed(Timing timing) {
		return timing == this.timing ? this : new SelectionImpl(this.owner, this.start, this.end, timing, this.operations);
	}
}
