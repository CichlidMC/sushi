package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.RegisteredTransformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.util.LazyClassModel;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.Set;

public final class TransformLookup {
	private final Map<ClassDesc, Set<PreparedTransform>> byTargetClass;
	private final Set<PreparedTransform> global;
	private final Comparator<PreparedTransform> comparator;

	public TransformLookup(SequencedMap<Id, Phase> phases) {
		this.byTargetClass = new HashMap<>();
		this.global = new HashSet<>();

		this.comparator = createComparator(phases.sequencedValues());

		for (Phase phase : phases.values()) {
			for (RegisteredTransformer transformer : phase.transformers().values()) {
				if (!transformer.isEnabled())
					continue;

				transformer.configured().transformer().register((target, transform) -> {
					PreparedTransform prepared = new PreparedTransform(transformer, target, transform);
					Optional<Set<ClassDesc>> concreteTargets = target.concreteMatches();
					if (concreteTargets.isEmpty()) {
						this.global.add(prepared);
					} else {
						for (ClassDesc desc : concreteTargets.get()) {
							this.byTargetClass.computeIfAbsent(desc, _ -> new HashSet<>()).add(prepared);
						}
					}
				});
			}
		}
	}

	public List<TransformStep> get(LazyClassModel model) {
		List<PreparedTransform> transforms = this.getTransforms(model);
		if (transforms.isEmpty())
			return List.of();

		List<TransformStep> steps = new ArrayList<>();
		TransformStep currentStep = newStep();
		steps.add(currentStep);
		Phase currentPhase = transforms.getFirst().owner.phase();

		for (PreparedTransform transform : transforms) {
			Phase newPhase = transform.owner.phase();

			if (currentPhase != newPhase) {
				// phase changed
				if (currentPhase.barriers().after || newPhase.barriers().before) {
					// there's a barrier between them, start a new step
					currentStep = newStep();
					steps.add(currentStep);
				}

				currentPhase = newPhase;
			}

			currentStep.phases().computeIfAbsent(newPhase, _ -> new LinkedHashSet<>()).add(transform);
		}

		return steps;
	}

	private List<PreparedTransform> getTransforms(LazyClassModel model) {
		List<PreparedTransform> transforms = new ArrayList<>();

		for (PreparedTransform transform : this.global) {
			if (transform.target.shouldApply(model.get())) {
				transforms.add(transform);
			}
		}

		Set<PreparedTransform> byTarget = this.byTargetClass.get(model.desc());
		if (byTarget != null) {
			for (PreparedTransform transform : byTarget) {
				if (transform.target.shouldApply(model.get())) {
					transforms.add(transform);
				}
			}
		}

		transforms.sort(this.comparator);
		return transforms;
	}

	private static TransformStep newStep() {
		return new TransformStep(new LinkedHashMap<>());
	}

	private static Comparator<PreparedTransform> createComparator(SequencedCollection<Phase> phases) {
		Map<Phase, Integer> indices = getIndices(phases);

		Comparator<Phase> phaseComparator = Comparator.comparingInt(phase -> {
			Integer index = indices.get(phase);
			return Objects.requireNonNull(index, () -> "Missing index for phase " + phase.id());
		});

		return (transformA, transformB) -> {
			int byPhase = phaseComparator.compare(transformA.owner.phase(), transformB.owner.phase());
			return byPhase != 0 ? byPhase : transformA.owner.compareTo(transformB.owner);
		};
	}

	private static <T> Map<T, Integer> getIndices(SequencedCollection<T> collection) {
		Map<T, Integer> map = new HashMap<>();
		int i = 0;

		for (T entry : collection) {
			map.put(entry, i);
			i++;
		}

		return map;
	}
}
