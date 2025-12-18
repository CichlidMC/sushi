package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.LongRefImpl;

/// Primitive `long`-specialized variant of [ObjectRef].
public sealed interface LongRef permits LongRefImpl {
	long get();

	void set(long value);
}
