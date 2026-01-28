package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.fishflakes.api.DirectedGraph;
import fish.cichlidmc.fishflakes.api.Either;
import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.detail.Detail;
import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.metadata.TransformedBy;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.api.transformer.phase.PhaseCycleException;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.condition.ConditionContextImpl;
import fish.cichlidmc.sushi.impl.transformer.lookup.SingleStepTransform;
import fish.cichlidmc.sushi.impl.transformer.lookup.TransformLookup;
import fish.cichlidmc.sushi.impl.transformer.lookup.TransformStep;
import fish.cichlidmc.sushi.impl.transformer.phase.MutablePhaseImpl;
import fish.cichlidmc.sushi.impl.transformer.phase.PhaseBuilderImpl;
import fish.cichlidmc.sushi.impl.util.LazyClassModel;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.AnnotationValue;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.SequencedSet;

public final class TransformerManagerImpl implements TransformerManager {
	private final Map<Id, ConfiguredTransformer> transformers;
	private final SequencedMap<Id, Phase> phases;
	private final boolean addMetadata;
	private final TransformLookup lookup;

	public TransformerManagerImpl(Map<Id, ConfiguredTransformer> transformers, SequencedMap<Id, Phase> phases, boolean addMetadata) {
		this.transformers = Collections.unmodifiableMap(transformers);
		this.phases = Collections.unmodifiableSequencedMap(phases);
		this.addMetadata = addMetadata;
		this.lookup = new TransformLookup(this.phases);
	}

	@Override
	public Optional<TransformResult> transform(ClassFile context, byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) {
		LazyClassModel lazyModel = new LazyClassModel(desc, () -> context.parse(bytes));
		Detail.Provider detail = Detail.Provider.of(() -> ClassDescs.fullName(lazyModel.desc()));
		return Details.with("Class being transformed", detail, TransformException::new, () -> {
			List<TransformStep> steps = this.lookup.get(lazyModel);
			if (steps.isEmpty()) {
				return Optional.empty();
			}

			ClassTransform tail = this.getTailTransform(steps, transform);
			ClassModel model = lazyModel.get();
			Transformation transformation = new Transformation(context, this.addMetadata, model);

			for (int i = 0; i < steps.size(); i++) {
				boolean last = i + 1 == steps.size();
				byte[] result = runStep(steps, i, transformation, last ? tail : null);

				if (last) {
					return Optional.of(new TransformResult(result, transformation.requirements.build()));
				} else {
					transformation.update(result);
				}
			}

			throw new IllegalStateException("This should never be reached! Phases: " + steps);
		});
	}

	@Nullable
	private ClassTransform getTailTransform(List<TransformStep> steps, @Nullable ClassTransform transform) {
		if (!this.addMetadata)
			return transform;

		ClassTransform metadata = createMetadataApplicator(steps);
		return transform == null ? metadata : metadata.andThen(transform);
	}

	@Override
	public Map<Id, ConfiguredTransformer> transformers() {
		return this.transformers;
	}

	@Override
	public SequencedMap<Id, Phase> phases() {
		return this.phases;
	}

	private static byte[] runStep(List<TransformStep> steps, int index, Transformation transformation, @Nullable ClassTransform andThen) {
		ClassTransform transform = new SingleStepTransform(transformation.clazz(), steps, index);
		ClassTransform finalTransform = andThen == null ? transform : transform.andThen(andThen);
		return transformation.context.transformClass(transformation.clazz().model(), finalTransform);
	}

	private static ClassTransform createMetadataApplicator(List<TransformStep> steps) {
		AnnotationValue[] lines = steps.stream()
				.flatMap(step -> step.transforms().stream())
				.map(transform -> transform.owner.id().toString())
				.map(AnnotationValue::ofString)
				.toArray(AnnotationValue[]::new);

		return Annotations.runtimeVisibleClassModifier(annotations -> annotations.addFirst(
				new Annotations.Entry(ClassDescs.of(TransformedBy.class))
						.put("value", AnnotationValue.ofArray(lines))
		));
	}

	public static final class BuilderImpl implements TransformerManager.Builder {
		private final Map<Id, ConfiguredTransformer> transformers = new HashMap<>();
		private final Map<Id, PhaseBuilderImpl> phases = new HashMap<>();
		private final MutablePhaseImpl defaultPhase = new MutablePhaseImpl(Phase.DEFAULT, this.transformers);
		private boolean addMetadata = true;

		@Override
		public Phase.Mutable defaultPhase() {
			return this.defaultPhase;
		}

		@Override
		public Optional<Phase.Builder> definePhase(Id id) throws IllegalArgumentException {
			if (id.equals(Phase.DEFAULT)) {
				throw new IllegalArgumentException("The default phase cannot be redefined");
			} else if (this.phases.containsKey(id)) {
				return Optional.empty();
			}

			PhaseBuilderImpl builder = new PhaseBuilderImpl(id, this.transformers);
			this.phases.put(id, builder);
			return Optional.of(builder);
		}

		@Override
		public Phase.Builder definePhaseOrThrow(Id id) throws IllegalArgumentException {
			return this.definePhase(id).orElseThrow(() -> new IllegalArgumentException("Phase already exists: " + id));
		}

		@Override
		public Builder addMetadata(boolean value) {
			this.addMetadata = value;
			return this;
		}

		@Override
		public TransformerManager build() throws PhaseCycleException {
			SequencedMap<Id, Phase> phases = this.computePhases();
			return new TransformerManagerImpl(Map.copyOf(this.transformers), phases, this.addMetadata);
		}

		private SequencedMap<Id, Phase> computePhases() throws PhaseCycleException {
			// this graph uses IDs instead of phases, since we want to also account for phases that
			// are referenced, but not directly defined, like if they come from an external source
			DirectedGraph<Id> phaseGraph = DirectedGraph.create(Comparator.naturalOrder());
			phaseGraph.addNode(Phase.DEFAULT);
			this.phases.values().forEach(phase -> phase.addToGraph(phaseGraph));

			Condition.Context ctx = new ConditionContextImpl(this.transformers.keySet());

			return switch (phaseGraph.sort()) {
				case Either.Right(DirectedGraph.Cycle<Id> cycle) -> throw new PhaseCycleException(cycle.elements());
				case Either.Left(SequencedSet<Id> ids) -> {
					SequencedMap<Id, Phase> map = new LinkedHashMap<>();
					for (Id id : ids) {
						MutablePhaseImpl phase = id.equals(Phase.DEFAULT) ? this.defaultPhase : this.phases.get(id);
						// IDs may refer to unknown phases, see above
						if (phase != null) {
							map.put(id, phase.build(ctx));
						}
					}
					yield map;
				}
			};
		}
	}
}
