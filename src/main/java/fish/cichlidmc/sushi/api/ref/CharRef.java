package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.CharRefImpl;

/// Primitive `char`-specialized variant of [ObjectRef].
public sealed interface CharRef permits CharRefImpl {
	char get();

	void set(char value);
}
