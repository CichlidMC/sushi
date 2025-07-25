package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl.Extraction;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SelectionApplicator implements CodeTransform {
	private final List<CodeElement> elements;
	private final Operations operations;
	private final Consumer<MethodEntry> methodGenerator;

	private SelectionApplicator(List<CodeElement> elements, Operations operations, Consumer<MethodEntry> methodGenerator) {
		this.elements = elements;
		this.operations = operations;
		this.methodGenerator = methodGenerator;
	}

	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
	}

	@Override
	public void atStart(CodeBuilder builder) {
		State state = new State();

		// TODO: extractions
		for (CodeElement element : this.elements) {
			// skip over everything that's not an instruction
			if (!(element instanceof Instruction) && !(element instanceof PseudoInstruction)) {
				builder.with(element);
				continue;
			}

			this.handlePoint(Point.before(element), state, builder);

			if (state.replacement == null) {
				if (state.extraction != null) {
					state.extraction.elements.add(new ExtractedElement.Code(element));
				} else {
					builder.with(element);
				}
			}

			this.handlePoint(Point.after(element), state, builder);
		}
	}

	private void handlePoint(Point point, State state, CodeBuilder builder) {
		// check if current replacement is done
		if (state.replacement != null && state.replacement.until.equals(point)) {
			state.replacement = null;
		}

		// check for a new replacement
		Replacement replacement = this.operations.replacements.get(point);
		if (replacement != null) {
			// if there's an overlap something has gone wrong
			if (state.replacement != null) {
				throw new IllegalStateException("Replacement overlap");
			}

			// it's possible the replacement selection has a length of 0
			if (!replacement.until.equals(point)) {
				state.replacement = replacement;
			}

			replacement.code.write(builder);
		}

		// insertions
		for (CodeBlock insertion : this.operations.insertions.getOrDefault(point, List.of())) {
			insertion.write(builder);
		}
	}

	public static SelectionApplicator create(List<CodeElement> elements, List<SelectionImpl> selections, Consumer<MethodEntry> methodGenerator) {
		Operations operations = new Operations();

		for (SelectionImpl selection : selections) {
			if (selection.replacement != null) {
				operations.replacements.put(selection.start(), new Replacement(selection.end(), selection.replacement));
			} else if (selection.extraction != null) {
				operations.extractions.computeIfAbsent(selection.start(), $ -> new ArrayList<>()).add(selection.extraction);
			}

			operations.maybeAdd(selection.start(), selection.insertionsBefore);
			operations.maybeAdd(selection.end(), selection.insertionsAfter);
		}

		return new SelectionApplicator(elements, operations, methodGenerator);
	}

	private record Operations(Map<Point, List<CodeBlock>> insertions,
							  Map<Point, Replacement> replacements,
							  Map<Point, List<Extraction>> extractions) {
		private Operations() {
			this(new HashMap<>(), new HashMap<>(), new HashMap<>());
		}

		private void maybeAdd(Point point, List<CodeBlock> insertions) {
			if (!insertions.isEmpty()) {
				this.insertions.computeIfAbsent(point, $ -> new ArrayList<>()).addAll(insertions);
			}
		}
	}

	private record Replacement(Point until, CodeBlock code) {
	}

	private static final class State {
		@Nullable
		private Replacement replacement;
		@Nullable
		private OngoingExtraction extraction;
	}

	private record OngoingExtraction(Point until, Extraction extraction, List<ExtractedElement> elements) {
	}

	private sealed interface ExtractedElement {
		record Code(CodeElement element) implements ExtractedElement {}
		record Block(CodeBlock block) implements ExtractedElement {}
	}
}
