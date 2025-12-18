package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.FloatRefImpl;

/// Primitive `float`-specialized variant of [ObjectRef].
public sealed interface FloatRef permits FloatRefImpl {
	float get();

	void set(float value);
}
