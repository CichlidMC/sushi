package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.runtime.ref.CharRefImpl;

/**
 * Primitive {@code char}-specialized variant of {@link ObjectRef}.
 */
public sealed interface CharRef permits CharRefImpl {
	char get();

	void set(char value);
}
