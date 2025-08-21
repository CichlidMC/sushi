package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.runtime.ref.LongRefImpl;

/**
 * Primitive {@code long}-specialized variant of {@link ObjectRef}.
 */
public sealed interface LongRef permits LongRefImpl {
	long get();

	void set(long value);
}
