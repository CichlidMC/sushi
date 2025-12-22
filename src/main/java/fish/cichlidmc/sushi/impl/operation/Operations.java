package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection.Timing;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.operation.apply.ApplicatorTransform;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;

import java.lang.classfile.CodeTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/// Records operations that have been registered by transforms.
public final class Operations {
	private final Map<Point, List<Insertion>> insertions;
	private final Map<Point, Replacement> replacements;
	private final Map<Point, List<Extraction>> extractions;

	public Operations() {
		this.insertions = new HashMap<>();
		this.replacements = new HashMap<>();
		this.extractions = new HashMap<>();
	}

	public void add(Insertion insertion) {
		List<Insertion> list = this.insertions.computeIfAbsent(insertion.point(), _ -> new ArrayList<>());
		insert(list, insertion, Insertion::timing);
	}

	public void add(Replacement operation) {
		Replacement existing = this.replacements.get(operation.from());
		if (existing != null) {
			throw new TransformException("Two transformers tried to replace the same code: " + existing.owner() + ", " + operation.owner());
		}

		this.replacements.put(operation.from(), operation);
	}

	public void add(Extraction extraction) {
		List<Extraction> list = this.extractions.computeIfAbsent(extraction.from(), _ -> new ArrayList<>());
		insert(list, extraction, Extraction::timing);
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
				if (replacement.conflictsWith(insertion)) {
					throw TransformException.of("Replacement would overwrite an Insertion", e -> {
						e.addDetail("Replacement Owner", replacement.owner());
						e.addDetail("Insertion Owner", insertion.owner());
					});
				}
			});

			for (Replacement other : this.replacements.values()) {
				if (replacement != other && replacement.conflictsWith(other)) {
					throw TransformException.of("Two Replacements attempt to overwrite the same code", e -> {
						e.addDetail("First Replacement Owner", replacement.owner());
						e.addDetail("Second Replacement Owner", other.owner());
					});
				}
			}

			this.forEachExtraction(extraction -> {
				if (replacement.conflictsWith(extraction)) {
					throw TransformException.of("Replacement and Extraction partially intersect", e -> {
						e.addDetail("Replacement Owner", replacement.owner());
						e.addDetail("Extraction Owner", extraction.owner());
					});
				}
			});
		}

		// extractions are safe, unless another extraction intersects it, or a replacement overlaps with it.
		// replacements have already been checked though.
		this.forEachExtraction(extraction -> this.forEachExtraction(other -> {
			if (extraction != other && extraction.conflictsWith(other)) {
				throw TransformException.of("Two Extractions partially intersect", e -> {
					e.addDetail("First Extraction Owner", extraction.owner());
					e.addDetail("Second Extraction Owner", other.owner());
				});
			}
		}));
	}

	/// Inserts `entry` into `list` as far into it as possible, while maintaining timings.
	private static <T> void insert(List<T> list, T entry, Function<T, Timing> timingFunction) {
		Timing timing = timingFunction.apply(entry);

		// find first entry with a timing that comes later
		for (int i = 0; i < list.size(); i++) {
			T t = list.get(i);
			Timing thisTiming = timingFunction.apply(t);
			if (thisTiming.comesAfter(timing)) {
				// found it
				list.add(i, entry);
				return;
			}
		}

		// found none, add to end
		list.addLast(entry);
	}

	/// All maps are mutable so operations can be removed as they're used.
	public record Validated(Map<Point, List<Insertion>> insertions, Map<Point, Replacement> replacements, Map<Point, List<Extraction>> extractions) {
		private Validated(Operations operations) {
			this(new HashMap<>(operations.insertions), new HashMap<>(operations.replacements), new HashMap<>(operations.extractions));
		}
	}
}
