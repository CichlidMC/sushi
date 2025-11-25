package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.InstructionComparisons;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.operation.apply.ApplicatorTransform;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;
import org.glavo.classfile.CodeTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Records operations that have been registered by transforms.
 */
public final class Operations {
	private final InstructionComparisons instructions;
	private final Map<Point, List<Insertion>> insertions;
	private final Map<Point, Replacement> replacements;
	private final Map<Point, List<Extraction>> extractions;

	public Operations(InstructionComparisons instructions) {
		this.instructions = instructions;
		this.insertions = new HashMap<>();
		this.replacements = new HashMap<>();
		this.extractions = new HashMap<>();
	}

	public void add(Insertion insertion) {
		this.insertions.computeIfAbsent(insertion.point(), $ -> new ArrayList<>()).add(insertion);
	}

	public void add(Replacement operation) {
		Replacement existing = this.replacements.get(operation.from());
		if (existing != null) {
			throw new TransformException("Two transformers tried to replace the same code: " + existing.owner() + ", " + operation.owner());
		}

		this.replacements.put(operation.from(), operation);
	}

	public void add(Extraction extraction) {
		this.extractions.computeIfAbsent(extraction.from(), $ -> new ArrayList<>()).add(extraction);
	}

	public Optional<CodeTransform> applicator(TransformableCodeImpl code, MethodGenerator methodGenerator) throws TransformException {
		if (this.insertions.isEmpty() && this.replacements.isEmpty() && this.extractions.isEmpty())
			return Optional.empty();

		this.checkForConflicts();
		Operations.Validated validated = new Validated(this);

		return Optional.of(new ApplicatorTransform(code, methodGenerator, validated));
	}

	private void forEachInsertion(Consumer<Insertion> consumer) {
		for (List<Insertion> list : this.insertions.values()) {
			list.forEach(consumer);
		}
	}

	private void forEachExtraction(Consumer<Extraction> consumer) {
		for (List<Extraction> list : this.extractions.values()) {
			list.forEach(consumer);
		}
	}

	private void checkForConflicts() throws TransformException {
		// insertions are safe. nothing to do for them alone.

		// replacements conflict when:
		// - an insertion is contained within
		// - another replacement intersects
		// - an extraction intersects or is contained
		for (Replacement replacement : this.replacements.values()) {
			this.forEachInsertion(insertion -> {
				if (replacement.conflictsWith(insertion, this.instructions)) {
					throw TransformException.of("Replacement would overwrite an Insertion", e -> {
						e.addDetail("Replacement Owner", replacement.owner());
						e.addDetail("Insertion Owner", insertion.owner());
					});
				}
			});

			for (Replacement other : this.replacements.values()) {
				if (replacement != other && replacement.conflictsWith(other, this.instructions)) {
					throw TransformException.of("Two Replacements attempt to overwrite the same code", e -> {
						e.addDetail("First Replacement Owner", replacement.owner());
						e.addDetail("Second Replacement Owner", other.owner());
					});
				}
			}

			this.forEachExtraction(extraction -> {
				if (replacement.conflictsWith(extraction, this.instructions)) {
					throw TransformException.of("Replacement and Extraction partially intersect", e -> {
						e.addDetail("Replacement Owner", replacement.owner());
						e.addDetail("Extraction Owner", extraction.owner());
					});
				}
			});
		}

		// extractions are safe, unless another extraction intersects it, or a replacement overlaps with it.
		// replacements have already been checked though.
		this.forEachExtraction(extraction -> {
			this.forEachExtraction(other -> {
				if (extraction != other && extraction.conflictsWith(other, this.instructions)) {
					throw TransformException.of("Two Extractions partially intersect", e -> {
						e.addDetail("First Extraction Owner", extraction.owner());
						e.addDetail("Second Extraction Owner", other.owner());
					});
				}
			});
		});
	}

	/**
	 * All maps are mutable so operations can be removed as they're used.
	 */
	public record Validated(Map<Point, List<Insertion>> insertions, Map<Point, Replacement> replacements, Map<Point, List<Extraction>> extractions) {
		private Validated(Operations operations) {
			this(new HashMap<>(operations.insertions), new HashMap<>(operations.replacements), new HashMap<>(operations.extractions));
		}
	}
}
