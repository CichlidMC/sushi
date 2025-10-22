package fish.cichlidmc.sushi.api.registry;

import fish.cichlidmc.sushi.impl.registry.SimpleRegistryImpl;
import fish.cichlidmc.tinycodecs.Codec;
import org.jetbrains.annotations.Nullable;

/**
 * A simple bidirectional mapping between entries and IDs.
 */
public sealed interface SimpleRegistry<T> permits SimpleRegistryImpl {
	/**
	 * Register a new mapping.
	 * @throws IllegalArgumentException if a mapping for the given key already exists
	 */
	void register(Id id, T value) throws IllegalArgumentException;

	@Nullable
	T get(Id id);

	@Nullable
	Id getId(T value);

	Codec<T> byIdCodec();

	/**
	 * Create a new, empty registry.
	 * @param fallbackNamespace the fallback namespace to use when one isn't present when decoding via {@link #byIdCodec()}
	 */
	static <T> SimpleRegistry<T> create(@Nullable String fallbackNamespace) {
		return new SimpleRegistryImpl<>(fallbackNamespace);
	}
}
