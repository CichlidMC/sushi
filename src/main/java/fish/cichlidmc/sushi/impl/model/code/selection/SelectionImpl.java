package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.ExtractionCodeBlock;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.impl.operation.Extraction;
import fish.cichlidmc.sushi.impl.operation.Insertion;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.operation.Replacement;

import java.lang.constant.MethodTypeDesc;

public final class SelectionImpl implements Selection {
	public final Id owner;

	private final Point start;
	private final Point end;
	private final Operations operations;

	public SelectionImpl(Id owner, Point start, Point end, Operations operations) {
		this.owner = owner;
		this.start = start;
		this.end = end;
		this.operations = operations;
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
	public void insert(CodeBlock code, Offset offset) {
		Point point = switch (offset) {
			case BEFORE -> this.start;
			case AFTER -> this.end;
		};

		this.operations.add(new Insertion(point, code, this.owner));
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
	public void extract(String name, MethodTypeDesc desc, ExtractionCodeBlock block) {
		this.operations.add(new Extraction(this.start, this.end, name, desc, block, this.owner));
	}
}
