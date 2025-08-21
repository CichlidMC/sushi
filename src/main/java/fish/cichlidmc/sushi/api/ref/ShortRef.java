package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.runtime.ref.ShortRefImpl;

/**
 * Primitive {@code short}-specialized variant of {@link ObjectRef}.
 */
public sealed interface ShortRef permits ShortRefImpl {
	short get();

	void set(short value);
}
