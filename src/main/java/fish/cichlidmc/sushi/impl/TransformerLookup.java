package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.phase.TransformPhase;
import fish.cichlidmc.sushi.impl.util.LazyClassModel;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class TransformerLookup {
	private final Map<ClassDesc, List<Transformer>> byTargetClass;
	private final List<Transformer> global;
	private final Set<Id> allIds;

	public TransformerLookup(Map<ClassDesc, List<Transformer>> byTargetClass, List<Transformer> global) {
		this.byTargetClass = Collections.unmodifiableMap(byTargetClass);
		this.global = Collections.unmodifiableList(global);

		Set<Id> ids = new HashSet<>();
		this.global.forEach(transformer -> ids.add(transformer.id()));
		for (List<Transformer> list : this.byTargetClass.values()) {
			for (Transformer transformer : list) {
				ids.add(transformer.id());
			}
		}
		this.allIds = Collections.unmodifiableSet(ids);
	}

	/**
	 * @return the IDs of all loaded transformers
	 */
	public Set<Id> ids() {
		return this.allIds;
	}

	public List<TransformPhase> get(LazyClassModel model) {
		List<Transformer> transformers = this.getTransformers(model);
		if (transformers.isEmpty())
			return List.of();

		List<TransformPhase> phases = new ArrayList<>();
		splitByPhase(transformers, phases::add);
		phases.sort(TransformPhase.BY_VALUE);

		return phases;
	}

	private List<Transformer> getTransformers(LazyClassModel model) {
		List<Transformer> transformers = new ArrayList<>();

		for (Transformer transformer : this.global) {
			if (transformer.shouldApply(model.get())) {
				transformers.add(transformer);
			}
		}

		List<Transformer> byTarget = this.byTargetClass.get(model.desc());
		if (byTarget != null) {
			for (Transformer transformer : byTarget) {
				if (transformer.shouldApply(model.get())) {
					transformers.add(transformer);
				}
			}
		}

		return transformers;
	}

	private static void splitByPhase(List<Transformer> transformers, Consumer<TransformPhase> output) {
		Map<Integer, List<Transformer>> map = new HashMap<>();

		for (Transformer transformer : transformers) {
			map.computeIfAbsent(transformer.phase(), $ -> new ArrayList<>()).add(transformer);
		}

		map.forEach((phase, list) -> {
			list.sort(Comparator.naturalOrder());
			output.accept(new TransformPhase(phase, list));
		});
	}

	public static TransformerLookup of(Collection<Transformer> transformers) {
		Map<ClassDesc, List<Transformer>> byTargetClass = new HashMap<>();
		List<Transformer> global = new ArrayList<>();

		for (Transformer transformer : transformers) {
			transformer.concreteTargets().ifPresentOrElse(
					set -> {
						for (ClassDesc target : set) {
							byTargetClass.computeIfAbsent(target, $ -> new ArrayList<>()).add(transformer);
						}
					},
					() -> global.add(transformer)
			);
		}

		return new TransformerLookup(byTargetClass, global);
	}
}
