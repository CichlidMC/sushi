package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.DoubleRefImpl;

/// Primitive `double`-specialized variant of [ObjectRef].
public sealed interface DoubleRef permits DoubleRefImpl {
	double get();

	void set(double value);
}
