package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.tinycodecs.Codec;

import java.util.HashMap;
import java.util.Map;

public final class NameMapper<T> {
	public final Codec<T> codec;

	private final Map<String, T> byName;
	private final Map<T, String> names;

	public NameMapper() {
		this.codec = Codec.byName(this::getName, this::get);
		this.byName = new HashMap<>();
		this.names = new HashMap<>();
	}

	public void put(String name, T value) {
		this.byName.put(name, value);
		this.names.put(value, name);
	}

	private T get(String name) {
		return this.byName.get(name);
	}

	private String getName(T value) {
		return this.names.get(value);
	}

	public boolean containsName(String name) {
		return this.byName.containsKey(name);
	}

	public boolean contains(T value) {
		return this.names.containsKey(value);
	}
}
