package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.ObjectRefImpl;

/**
 * A mutable reference to an object.
 * <p>
 * Also comes in primitive-specialized variants; see package.
 * <p>
 * Instances of these interfaces have undefined lifecycles, and should never be retained.
 */
public sealed interface ObjectRef<T> permits ObjectRefImpl {
	T get();

	void set(T value);
}
