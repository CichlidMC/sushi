package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.metadata.TransformedBy;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.condition.ConditionContextImpl;
import fish.cichlidmc.sushi.impl.phase.TransformPhase;
import fish.cichlidmc.sushi.impl.util.LazyClassModel;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class TransformerManagerImpl implements TransformerManager {
	private final TransformerLookup transformers;
	private final Optional<Validation> validation;
	private final boolean addMetadata;

	public TransformerManagerImpl(TransformerLookup transformers, Optional<Validation> validation, boolean addMetadata) {
		this.transformers = transformers;
		this.validation = validation;
		this.addMetadata = addMetadata;
	}

	@Override
	public Optional<byte[]> transform(ClassFile context, byte[] bytes, @Nullable ClassDesc desc, @Nullable ClassTransform transform) {
		LazyClassModel lazyModel = new LazyClassModel(desc, () -> context.parse(bytes));
		return TransformException.withDetail("Class being Transformed", ClassDescs.fullName(lazyModel.desc()), () -> {
			List<TransformPhase> phases = this.transformers.get(lazyModel);
			if (phases.isEmpty()) {
				return Optional.empty();
			}

			ClassTransform tail = this.getTailTransform(phases, transform);
			ClassModel model = lazyModel.get();

			for (int i = 0; i < phases.size(); i++) {
				TransformPhase phase = phases.get(i);
				boolean last = i + 1 == phases.size();

				// final copy for the lambda
				ClassModel currentModel = model;

				byte[] transformed = TransformException.withDetail(
						"Phase", phase.phase(),
						() -> phase.transform(context, currentModel, this.validation, this.addMetadata, last ? tail : null)
				);

				if (last) {
					return Optional.of(transformed);
				} else {
					model = context.parse(transformed);
				}
			}

			throw new IllegalStateException("This should never be reached! Phases: " + phases);
		});
	}

	@Nullable
	private ClassTransform getTailTransform(List<TransformPhase> phases, @Nullable ClassTransform transform) {
		if (!this.addMetadata)
			return transform;

		ClassTransform metadata = createMetadataApplicator(phases);
		return transform == null ? metadata : metadata.andThen(transform);
	}

	@Override
	public Set<Id> transformers() {
		return this.transformers.ids();
	}

	private static ClassTransform createMetadataApplicator(List<TransformPhase> phases) {
		AnnotationValue[] lines = phases.stream()
				.flatMap(phase -> phase.transformers().stream())
				.map(transformer -> transformer.id().toString())
				.map(AnnotationValue::ofString)
				.toArray(AnnotationValue[]::new);

		return Annotations.runtimeVisibleClassModifier(annotations -> annotations.addFirst(
				new Annotations.Entry(ClassDescs.of(TransformedBy.class))
						.put("value", AnnotationValue.ofArray(lines))
		));
	}

	public static final class BuilderImpl implements TransformerManager.Builder {
		private final Map<Id, Transformer> transformers = new HashMap<>();
		private final AttachmentMap conditionContextAttachments = AttachmentMap.create();
		private Optional<Validation> validation = Optional.empty();
		private boolean addMetadata = true;

		@Override
		public Optional<String> parseAndRegister(Id id, JsonValue json) {
			CodecResult<TransformerDefinition> result = TransformerDefinition.CODEC.decode(json);
			if (result.isError()) {
				return Optional.of(result.asError().message);
			}

			List<Transformer> transformers = result.getOrThrow().decompose(id);
			List<Id> conflicts = transformers.stream()
					.map(Transformer::id)
					.filter(this.transformers::containsKey)
					.toList();

			if (!conflicts.isEmpty()) {
				return Optional.of("Duplicate transformers with ID(s): " + conflicts);
			}

			transformers.forEach(transformer -> this.transformers.put(transformer.id(), transformer));
			return Optional.empty();
		}

		@Override
		public boolean register(Transformer transformer) {
			if (this.transformers.containsKey(transformer.id()))
				return false;

			this.transformers.put(transformer.id(), transformer);
			return true;
		}

		@Override
		public Builder registerOrThrow(Transformer transformer) throws IllegalArgumentException {
			if (!this.register(transformer)) {
				throw new IllegalArgumentException("Transformer is already registered: " + transformer);
			}

			return this;
		}

		@Override
		public Builder configureConditionContext(Consumer<AttachmentMap> consumer) {
			consumer.accept(this.conditionContextAttachments);
			return this;
		}

		@Override
		public Builder withValidation(@Nullable Validation validation) {
			this.validation = Optional.ofNullable(validation);
			return this;
		}

		@Override
		public Builder addMetadata(boolean value) {
			this.addMetadata = value;
			return this;
		}

		@Override
		public TransformerManager build() {
			// make a copy, map will be modified
			Set<Id> ids = Set.copyOf(this.transformers.keySet());
			Condition.Context ctx = new ConditionContextImpl(ids, this.conditionContextAttachments.immutable());
			this.transformers.values().removeIf(transformer -> {
				if (transformer.condition().isEmpty())
					return false;

				Condition condition = transformer.condition().get();
				return !condition.test(ctx);
			});

			TransformerLookup transformers = TransformerLookup.of(this.transformers.values());
			return new TransformerManagerImpl(transformers, this.validation, this.addMetadata);
		}
	}
}
