package fish.cichlidmc.sushi.api.util;

import fish.cichlidmc.sushi.impl.util.SimpleRegistryImpl;
import fish.cichlidmc.tinycodecs.Codec;
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
		return create(registry -> {});
	}

	static <T> SimpleRegistry<T> create(Bootstrap<T> bootstrap) {
		return SimpleRegistryImpl.create(bootstrap);
	}

	/**
	 * Builder for a SimpleRegistry, used in bootstrapping
	 */
	interface Builder<T> {
		void register(Id id, T value) throws IllegalArgumentException;

		/**
		 * Set the default namespace to use when converting Strings into IDs for this registry's {@link #byIdCodec()}.
		 */
		void setDefaultNamespace(String namespace);
	}

	interface Bootstrap<T> {
		void bootstrap(Builder<T> builder);
	}
}
