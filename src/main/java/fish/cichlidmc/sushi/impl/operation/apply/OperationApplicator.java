package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.impl.apply.MethodGenerator;
import fish.cichlidmc.sushi.impl.operation.Extraction;
import fish.cichlidmc.sushi.impl.operation.Insertion;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.operation.Replacement;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public final class OperationApplicator {
	private final CodeBuilder builder;
	private final List<CodeElement> elements;
	private final MethodGenerator methodGenerator;
	private final Operations.Validated operations;

	// state used during application

	@Nullable
	private Replacement replacement;
	private final Deque<Extractor> extractors;

	public OperationApplicator(CodeBuilder builder, List<CodeElement> elements, MethodGenerator methodGenerator, Operations.Validated operations) {
		this.builder = builder;
		this.elements = elements;
		this.methodGenerator = methodGenerator;
		this.operations = operations;

		this.extractors = new ArrayDeque<>();
	}

	public void run() {
		for (CodeElement element : this.elements) {
			// skip over everything that's not an instruction
			if (!(element instanceof Instruction) && !(element instanceof PseudoInstruction)) {
				this.builder.with(element);
				continue;
			}

			this.handlePoint(Point.before(element));

			// if there's a replacement in progress, discard the instruction
			if (this.replacement == null) {
				this.write(element);
			}

			this.handlePoint(Point.after(element));
		}

		// end of code, all operations should be complete and consumed.
		if (this.replacement != null) {
			throw new IllegalStateException("End of code reached, but a replacement is still in progress: " + this.replacement);
		} else if (!this.extractors.isEmpty()) {
			List<Extraction> extractions = this.extractors.stream().map(extractor -> extractor.extraction).toList();
			throw new IllegalStateException("End of code reached, but one or more extractions are still in progress: " + extractions);
		} else if (!this.operations.insertions().isEmpty()) {
			throw new IllegalStateException("One or more insertions never found their target: " + this.operations.insertions().values());
		} else if (!this.operations.replacements().isEmpty()) {
			throw new IllegalStateException("One or more replacements never found their target: " + this.operations.replacements());
		} else if (!this.operations.extractions().isEmpty()) {
			throw new IllegalStateException("One or more extractions never found their target: " + this.operations.extractions().values());
		}
	}

	private void handlePoint(Point point) {
		// 1: end replacement / extractions
		// 2: insertions
		// 3: new replacement / extractions

		// see if current replacement is done
		if (this.replacement != null && this.replacement.to().equals(point)) {
			this.replacement = null;
		}

		// end all current extractions that are now done
		while (!this.extractors.isEmpty()) {
			Extractor extractor = this.extractors.peek();
			if (extractor.extraction.to().equals(point)) {
				extractor.finish(this.builder, this.methodGenerator);
				this.extractors.pop();
			}
		}

		// add insertions
		this.consumeInsertions(point, insertion -> this.write(insertion.code()));

		// check for a new replacement
		Replacement replacement = this.operations.replacements().remove(point);
		if (replacement != null) {
			// if there's an overlap something has gone wrong, might as well double check this
			if (this.replacement != null) {
				throw new IllegalStateException("Replacement overlap! Something has gone very wrong! " + this.replacement + " / " + replacement);
			}

			// it's possible the replacement selection has a length of 0.
			// only set the current replacement if that's not the case.
			if (!replacement.to().equals(point)) {
				this.replacement = replacement;
			}

			this.write(replacement.replacement());
		}

		// check for new extractions
		List<Extraction> extractions = this.operations.extractions().remove(point);
		if (extractions != null) {
			for (Extraction extraction : extractions) {
				this.write(extraction.replacement());
				this.extractors.push(new Extractor(extraction));
			}
		}
	}

	private void consumeInsertions(Point point, Consumer<Insertion> consumer) {
		List<Insertion> insertions = this.operations.insertions().remove(point);
		if (insertions != null) {
			insertions.forEach(consumer);
		}
	}

	private void write(CodeElement instruction) {
		Extractor extractor = this.extractors.peek();
		if (extractor != null) {
			extractor.intercept(instruction);
		} else {
			this.builder.with(instruction);
		}
	}

	private void write(CodeBlock block) {
		Extractor extractor = this.extractors.peek();
		if (extractor == null) {
			block.write(this.builder);
			return;
		}

		CodeTransform collector = new CodeElementCollector(extractor::intercept);
		// this will execute the block and provide the generated elements to the extractor
		this.builder.transforming(collector, block::write);
	}
}
