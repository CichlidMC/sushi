package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.phase.TransformPhase;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.util.LazyClassModel;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class TransformLookup {
	public final Map<Id, ConfiguredTransformer> transformers;
	private final Map<ClassDesc, List<PreparedTransform>> byTargetClass;
	private final List<PreparedTransform> global;

	public TransformLookup(Map<Id, ConfiguredTransformer> transformers) {
		Map<ClassDesc, List<PreparedTransform>> byTargetClass = new HashMap<>();
		List<PreparedTransform> global = new ArrayList<>();

		for (ConfiguredTransformer transformer : transformers.values()) {
			transformer.transformer().register((target, transform) -> {
				PreparedTransform prepared = new PreparedTransform(transformer, target, transform);
				Optional<Set<ClassDesc>> concreteTargets = target.concreteTargets();
				if (concreteTargets.isEmpty()) {
					global.add(prepared);
				} else {
					for (ClassDesc desc : concreteTargets.get()) {
						byTargetClass.computeIfAbsent(desc, $ -> new ArrayList<>()).add(prepared);
					}
				}
			});
		}

		this.transformers = Collections.unmodifiableMap(transformers);
		this.byTargetClass = Collections.unmodifiableMap(byTargetClass);
		this.global = Collections.unmodifiableList(global);
	}

	public List<TransformPhase> get(LazyClassModel model) {
		List<PreparedTransform> transformers = this.getTransforms(model);
		if (transformers.isEmpty())
			return List.of();

		List<TransformPhase> phases = new ArrayList<>();
		splitByPhase(transformers, phases::add);
		phases.sort(TransformPhase.BY_VALUE);

		return phases;
	}

	private List<PreparedTransform> getTransforms(LazyClassModel model) {
		List<PreparedTransform> transforms = new ArrayList<>();

		for (PreparedTransform transform : this.global) {
			if (transform.target().shouldApply(model.get())) {
				transforms.add(transform);
			}
		}

		List<PreparedTransform> byTarget = this.byTargetClass.get(model.desc());
		if (byTarget != null) {
			for (PreparedTransform transform : byTarget) {
				if (transform.target().shouldApply(model.get())) {
					transforms.add(transform);
				}
			}
		}

		return transforms;
	}

	private static void splitByPhase(List<PreparedTransform> transforms, Consumer<TransformPhase> output) {
		Map<Integer, List<PreparedTransform>> map = new HashMap<>();

		for (PreparedTransform transform : transforms) {
			map.computeIfAbsent(transform.owner().phase(), $ -> new ArrayList<>()).add(transform);
		}

		map.forEach((phase, list) -> {
			list.sort(Comparator.naturalOrder());
			output.accept(new TransformPhase(phase, list));
		});
	}
}
