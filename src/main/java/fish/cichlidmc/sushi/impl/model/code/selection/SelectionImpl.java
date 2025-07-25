package fish.cichlidmc.sushi.impl.model.code.selection;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.util.Id;
import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SelectionImpl implements Selection {
	public final Id owner;

	private final Point start;
	private final Point end;
	private final InstructionList instructions;
	public final List<CodeBlock> insertionsBefore;
	public final List<CodeBlock> insertionsAfter;

	public CodeBlock replacement;
	public Extraction extraction;

	public SelectionImpl(Id owner, Point start, Point end, InstructionList instructions) {
		this.owner = owner;
		this.start = start;
		this.end = end;
		this.instructions = instructions;
		this.insertionsBefore = new ArrayList<>();
		this.insertionsAfter = new ArrayList<>();
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
	public void insertBefore(CodeBlock code) {
		this.insertionsBefore.add(code);
	}

	@Override
	public void insertAfter(CodeBlock code) {
		this.insertionsAfter.add(code);
	}

	@Override
	public void replace(CodeBlock code) {
		if (this.extraction != null) {
			throw new IllegalArgumentException("A selection cannot be both replaced and extracted.");
		}

		this.replacement = code;
		this.insertionsBefore.clear();
		this.insertionsAfter.clear();
	}

	@Override
	public void extract(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> init, CodeBlock header, CodeBlock footer, CodeBlock replacement) {
		if (this.replacement != null) {
			throw new IllegalArgumentException("A selection cannot be both replaced and extracted.");
		}

		this.extraction = new Extraction(name, desc, flags, init, header, footer, replacement);
	}

	// this is called bidirectionally
	public boolean conflictsWith(SelectionImpl that) {
		if (this.replacement == null && this.extraction == null)
			return false;

		boolean containsStart = this.contains(that.start);
		boolean containsEnd = this.contains(that.end);

		if (this.replacement != null) {
			// replacements must not intersect other selections at all
			return containsStart || containsEnd;
		} else if (this.extraction != null) {
			// extractions can fully contain other selections
			return containsStart ^ containsEnd;
		} else {
			throw new IllegalStateException("Replacement and extraction are both null?");
		}
	}

	private boolean contains(Point point) {
		return this.instructions.compare(this.start, point) < 0 && this.instructions.compare(this.end, point) > 0;
	}

	public record Extraction(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> init, CodeBlock header, CodeBlock footer, CodeBlock replacement) {
	}
}
