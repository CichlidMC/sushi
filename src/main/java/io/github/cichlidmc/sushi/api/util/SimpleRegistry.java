package io.github.cichlidmc.sushi.api.util;

import io.github.cichlidmc.sushi.impl.util.SimpleRegistryImpl;
import io.github.cichlidmc.tinycodecs.Codec;
import org.jetbrains.annotations.Nullable;

public interface SimpleRegistry<T> {
	/**
	 * Register a new mapping.
	 * @throws IllegalArgumentException if a mapping for the given key already exists
	 */
	void register(Id id, T value) throws IllegalArgumentException;

	@Nullable
	T get(Id id);

	Codec<T> byIdCodec();

	static <T> SimpleRegistry<T> create() {
		return new SimpleRegistryImpl<>();
	}
}
