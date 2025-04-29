package fish.cichlidmc.sushi.impl.util;

import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class SimpleRegistryImpl<T> implements SimpleRegistry<T> {
	private final Map<Id, T> map;
	private final Map<T, Id> reverseMap;
	private final Codec<T> codec;

	private SimpleRegistryImpl(Map<Id, T> map, Map<T, Id> reverseMap, String defaultNamespace) {
		this.map = map;
		this.reverseMap = reverseMap;
		this.codec = Id.fallbackNamespaceCodec(defaultNamespace).flatXmap(this::decode, this::encode);
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
		return this.map.get(id);
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
		Id id = this.reverseMap.get(value);
		if (id != null) {
			return CodecResult.success(id);
		} else {
			return CodecResult.error("Unknown object: " + value);
		}
	}

	private static final class BuilderImpl<T> implements Builder<T> {
		private final Map<Id, T> map = new HashMap<>();
		private final Map<T, Id> reverseMap = new IdentityHashMap<>();
		private String defaultNamespace = Id.BUILT_IN_NAMESPACE;

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
		public void setDefaultNamespace(String namespace) {
			this.defaultNamespace = Objects.requireNonNull(namespace);
		}

		private SimpleRegistry<T> build() {
			return new SimpleRegistryImpl<>(this.map, this.reverseMap, this.defaultNamespace);
		}
	}

	public static <T> SimpleRegistry<T> create(Bootstrap<T> bootstrap) {
		BuilderImpl<T> builder = new BuilderImpl<>();
		bootstrap.bootstrap(builder);
		return builder.build();
	}
}
