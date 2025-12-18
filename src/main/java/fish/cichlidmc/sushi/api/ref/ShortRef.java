package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.ShortRefImpl;

/// Primitive `short`-specialized variant of [ObjectRef].
public sealed interface ShortRef permits ShortRefImpl {
	short get();

	void set(short value);
}
