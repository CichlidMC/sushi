package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.DoubleRefImpl;

/**
 * Primitive {@code double}-specialized variant of {@link ObjectRef}.
 */
public sealed interface DoubleRef permits DoubleRefImpl {
	double get();

	void set(double value);
}
