package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.tinycodecs.DecodeResult;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TransformerManagerImpl implements TransformerManager {
	private final Map<Id, Transformer> transformers;

	public TransformerManagerImpl(Map<Id, Transformer> transformers) {
		this.transformers = Collections.unmodifiableMap(new HashMap<>(transformers));
	}

	@Override
	public boolean transform(ClassNode node) throws TransformException {
		if (this.transformers.isEmpty())
			return false;

		boolean transformed = false;
		for (Transformer transformer : this.transformers.values()) {
			transformed |= transformer.apply(node);
		}
		return transformed;
	}

	public static final class BuilderImpl implements TransformerManager.Builder {
		private final Map<Id, Transformer> transformers = new HashMap<>();

		@Override
		public Optional<String> parseAndRegister(Id id, JsonValue json) {
			DecodeResult<TransformerInstance> result = TransformerInstance.CODEC.decode(json);
			if (result.isError()) {
				String message = ((DecodeResult.Error<TransformerInstance>) result).message;
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
		public TransformerManager build() {
			return new TransformerManagerImpl(this.transformers);
		}
	}
}
