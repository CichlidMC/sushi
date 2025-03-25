package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.tinycodecs.CodecResult;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TransformerManagerImpl implements TransformerManager {
	private final List<Transformer> transformers;
	@Nullable
	private final Path outputDir;

	public TransformerManagerImpl(Map<Id, Transformer> transformers, @Nullable Path outputDir) {
		this.transformers = new ArrayList<>(transformers.values());
		this.transformers.sort(Transformer.PRIORITY_COMPARATOR);
		this.outputDir = outputDir;
	}

	@Override
	public boolean transform(ClassNode node, @Nullable ClassReader reader) throws TransformException {
		if (this.transformers.isEmpty())
			return false;

		boolean transformed = false;
		for (Transformer transformer : this.transformers) {
			transformed |= transformer.apply(node);
		}

		if (this.outputDir != null && transformed) {
			Path path = this.outputDir.resolve(node.name + ".class");
			this.writeTransformedClass(node, path, reader);
		}

		return transformed;
	}

	private void writeTransformedClass(ClassNode node, Path path, @Nullable ClassReader reader) {
		ClassWriter writer = new ClassWriter(reader, 0);
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

		@Override
		public Optional<String> parseAndRegister(Id id, JsonValue json) {
			CodecResult<TransformerDefinition> result = TransformerDefinition.CODEC.decode(json);
			if (result.isError()) {
				String message = result.asError().message;
				return Optional.of(message);
			}

			Transformer existing = this.transformers.get(id);
			if (existing != null) {
				return Optional.of("Duplicate transformers with ID: " + id);
			}

			this.transformers.put(id, new Transformer(id, result.getOrThrow()));
			return Optional.empty();
		}

		@Override
		public Builder output(Path path) {
			this.outputDir = path;
			return this;
		}

		@Override
		public TransformerManager build() {
			return new TransformerManagerImpl(this.transformers, this.outputDir);
		}
	}
}
