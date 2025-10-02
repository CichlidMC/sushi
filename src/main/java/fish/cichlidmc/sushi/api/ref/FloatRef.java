package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.FloatRefImpl;

/**
 * Primitive {@code float}-specialized variant of {@link ObjectRef}.
 */
public sealed interface FloatRef permits FloatRefImpl {
	float get();

	void set(float value);
}
