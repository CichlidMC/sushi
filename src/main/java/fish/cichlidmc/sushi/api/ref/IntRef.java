package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.IntRefImpl;

/// Primitive `int`-specialized variant of [ObjectRef].
public sealed interface IntRef permits IntRefImpl {
	int get();

	void set(int value);
}
