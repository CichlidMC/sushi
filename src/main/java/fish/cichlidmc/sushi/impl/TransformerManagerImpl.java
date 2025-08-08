package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.LazyClassModel;
import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.util.Utils;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.apply.ManagedTransform;
import fish.cichlidmc.sushi.impl.apply.MetadataApplicator;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TransformerManagerImpl implements TransformerManager {
	private final Map<ClassDesc, List<Transformer>> byTargetClass;
	private final List<Transformer> global;
	private final Optional<Validation> validation;
	private final boolean addMetadata;

	public TransformerManagerImpl(Collection<Transformer> transformers, Optional<Validation> validation, boolean addMetadata) {
		this.byTargetClass = new HashMap<>();
		this.global = new ArrayList<>();
		this.validation = validation;
		this.addMetadata = addMetadata;

		// add each transformer to either the per-class lists or the global list
		for (Transformer transformer : transformers) {
			Optional<Set<ClassDesc>> concrete = transformer.concreteTargets();
			if (concrete.isPresent()) {
				for (ClassDesc desc : concrete.get()) {
					this.byTargetClass.computeIfAbsent(desc, $ -> new ArrayList<>()).add(transformer);
				}
			} else {
				this.global.add(transformer);
			}
		}

		// pre-sort the lists since they might be used directly
		this.byTargetClass.values().forEach(list -> list.sort(Comparator.naturalOrder()));
		this.global.sort(Comparator.naturalOrder());
	}

	@Override
	public Optional<ClassTransform> transformFor(LazyClassModel clazz) {
		// also checks shouldApply
		List<Transformer> transformers = this.getTransformersForClass(clazz);
		if (transformers.isEmpty()) {
			return Optional.empty();
		}

		TransformContextImpl context = new TransformContextImpl(clazz.get(), this.validation);
		ClassTransform transform = new ManagedTransform(transformers, context);

		if (this.addMetadata) {
			transform = transform.andThen(new MetadataApplicator(transformers));
		}

		return Optional.of(transform);
	}

	private List<Transformer> getTransformersForClass(LazyClassModel clazz) {
		return Utils.merge(this.global, this.byTargetClass.get(clazz.desc())).stream()
				.filter(transformer -> transformer.target.shouldApply(clazz.get()))
				.toList();
	}

	public static final class BuilderImpl implements TransformerManager.Builder {
		private final Map<Id, Transformer> transformers = new HashMap<>();
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
					.map(transformer -> transformer.id)
					.filter(this.transformers::containsKey)
					.toList();

			if (!conflicts.isEmpty()) {
				return Optional.of("Duplicate transformers with ID(s): " + conflicts);
			}

			transformers.forEach(transformer -> this.transformers.put(transformer.id, transformer));
			return Optional.empty();
		}

		@Override
		public boolean register(Transformer transformer) {
			if (this.transformers.containsKey(transformer.id))
				return false;

			this.transformers.put(transformer.id, transformer);
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
			return new TransformerManagerImpl(this.transformers.values(), this.validation, this.addMetadata);
		}
	}
}
