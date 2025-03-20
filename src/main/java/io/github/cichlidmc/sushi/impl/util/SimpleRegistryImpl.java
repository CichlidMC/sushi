package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.DecodeResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class SimpleRegistryImpl<T> implements SimpleRegistry<T> {
	private final Map<Id, T> map = new HashMap<>();
	private final Map<T, Id> reverseMap = new IdentityHashMap<>();
	private final Codec<T> codec = Id.CODEC.flatXmap(this::decode, this::encode);

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
		return this.map.get(id);
	}

	@Override
	public Codec<T> byIdCodec() {
		return this.codec;
	}

	private DecodeResult<T> decode(Id id) {
		T value = this.get(id);
		if (value != null) {
			return DecodeResult.success(value);
		} else {
			return DecodeResult.error("Unknown ID: " + id);
		}
	}

	private Id encode(T value) {
		return this.reverseMap.get(value);
	}
}
