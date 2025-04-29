package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.util.Utils;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class TransformerManagerImpl implements TransformerManager {
	public static final String TRANSFORMED_PATH = "transformed";
	public static final String ERRORED_PATH = "errored";

	private final Map<String, List<Transformer>> byTargetClass;
	private final List<Transformer> global;
	@Nullable
	private final Path outputDir;
	private final boolean addMetadata;

	public TransformerManagerImpl(Collection<Transformer> transformers, @Nullable Path outputDir, boolean addMetadata) {
		this.byTargetClass = new HashMap<>();
		this.global = new ArrayList<>();
		this.outputDir = outputDir;
		this.addMetadata = addMetadata;

		// add each transformer to either the per-class lists or the global list
		for (Transformer transformer : transformers) {
			Optional<Set<String>> concrete = transformer.concreteTargets();
			if (concrete.isPresent()) {
				for (String className : concrete.get()) {
					this.byTargetClass.computeIfAbsent(className, $ -> new ArrayList<>()).add(transformer);
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
	public boolean transform(ClassNode node, @Nullable ClassReader reader) throws TransformException {
		List<Transformer> transformers = this.getTransformersForClass(node);
		if (transformers.isEmpty())
			return false;

		if (!this.handleTransform(transformers, node, reader))
			return false;

		if (this.addMetadata) {
			this.addMetadata(node, transformers);
		}

		if (this.outputDir != null) {
			Path path = this.outputDir.resolve(TRANSFORMED_PATH).resolve(node.name + ".class");
			writeClass(node, path, reader);
		}

		return true;
	}

	private boolean handleTransform(List<Transformer> transformers, ClassNode node, @Nullable ClassReader reader) throws TransformException {
		TransformContextImpl context = new TransformContextImpl(node, transformers);
		boolean transformed = false;

		try {
			while (context.hasNext()) {
				transformed |= this.handleNextTransformer(context);
			}
			return transformed;
		} catch (TransformException e) {
			// try to dump the current state for debugging
			if (this.outputDir != null) {
				try {
					Path path = this.outputDir.resolve(ERRORED_PATH).resolve(node.name + ".class");
					writeClass(node, path, reader);
				} catch (Throwable t) {
					e.addSuppressed(t);
				}
			}

			throw e;
		}
	}

	private boolean handleNextTransformer(TransformContextImpl context) {
		Transformer transformer = context.next();

		try {
			boolean transformed = transformer.transform.apply(context);
			context.finishCurrent();
			return transformed;
		} catch (TransformException e) {
			throw new TransformException("Error applying transformer " + transformer.id + " to class " + context.node().name, e);
		} catch (Throwable t) {
			throw new TransformException(
					"An unhandled exception occurred while applying transformer " + transformer.id + " to class " + context.node().name, t
			);
		}
	}

	private void addMetadata(ClassNode node, List<Transformer> transformers) {
		List<String> lines = transformers.stream()
				.map(transformer -> transformer.id + " - " + transformer.describe())
				.collect(Collectors.toList());

		if (node.visibleAnnotations == null) {
			node.visibleAnnotations = new ArrayList<>();
		}

		AnnotationNode annotation = new AnnotationNode(SushiInternals.METADATA_DESC);
		annotation.values = new ArrayList<>();
		annotation.values.add("value");
		annotation.values.add(lines);
		node.visibleAnnotations.add(annotation);
	}

	private List<Transformer> getTransformersForClass(ClassNode node) {
		List<Transformer> base = Utils.merge(this.global, this.byTargetClass.get(node.name));
		return base.isEmpty() ? base : base.stream()
				.filter(transformer -> transformer.target.shouldApply(node))
				.collect(Collectors.toList());
	}

	private static void writeClass(ClassNode node, Path path, @Nullable ClassReader reader) {
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);

		try {
			Files.createDirectories(path.getParent());
			Files.write(path, writer.toByteArray(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new RuntimeException("Failed to export transformed class", e);
		}
	}

	public static final class BuilderImpl implements TransformerManager.Builder {
		private final Map<Id, Transformer> transformers = new HashMap<>();
		@Nullable
		private Path outputDir;
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
					.collect(Collectors.toList());

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
		public Builder output(Path path) {
			this.outputDir = path;
			return this;
		}

		@Override
		public Builder addMetadata(boolean value) {
			this.addMetadata = value;
			return this;
		}

		@Override
		public TransformerManager build() {
			return new TransformerManagerImpl(this.transformers.values(), this.outputDir, this.addMetadata);
		}
	}
}
