package fish.cichlidmc.sushi.impl.registry;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.tinycodecs.api.CodecResult;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class SimpleRegistryImpl<T> implements SimpleRegistry<T> {
	private final Map<Id, T> map;
	private final Map<T, Id> reverseMap;
	private final Codec<T> codec;

	public SimpleRegistryImpl(@Nullable String fallbackNamespace) {
		this.map = new HashMap<>();
		this.reverseMap = new IdentityHashMap<>();
		this.codec = Id.fallbackNamespaceCodec(fallbackNamespace).flatXmap(this::decode, this::encode);
	}

	@Override
	public void register(Id id, T value) throws IllegalArgumentException {
		if (this.map.containsKey(id)) {
			throw new IllegalArgumentException("A mapping for id " + id + " already present");
		} else {
			this.map.put(id, value);
			this.reverseMap.put(value, id);
		}
	}

	@Override
	@Nullable
	public T get(Id id) {
		Objects.requireNonNull(id);
		return this.map.get(id);
	}

	@Nullable
	@Override
	public Id getId(T value) {
		Objects.requireNonNull(value);
		return this.reverseMap.get(value);
	}

	@Override
	public Codec<T> byIdCodec() {
		return this.codec;
	}

	private CodecResult<T> decode(Id id) {
		T value = this.get(id);
		if (value != null) {
			return CodecResult.success(value);
		} else {
			return CodecResult.error("Unknown ID: " + id);
		}
	}

	private CodecResult<Id> encode(T value) {
		Id id = this.getId(value);
		if (id != null) {
			return CodecResult.success(id);
		} else {
			return CodecResult.error("Unknown object: " + value);
		}
	}
}
