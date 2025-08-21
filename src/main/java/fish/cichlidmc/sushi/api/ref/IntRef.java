package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.runtime.ref.IntRefImpl;

/**
 * Primitive {@code int}-specialized variant of {@link ObjectRef}.
 */
public sealed interface IntRef permits IntRefImpl {
	int get();

	void set(int value);
}
