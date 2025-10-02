package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.BoolRefImpl;

/**
 * Primitive {@code boolean}-specialized variant of {@link ObjectRef}.
 */
public sealed interface BoolRef permits BoolRefImpl {
	boolean get();

	void set(boolean value);
}
