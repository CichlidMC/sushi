package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.runtime.ref.ObjectRefImpl;

/**
 * A mutable reference to an object.
 * <p>
 * Also comes in primitive-specialized variants; see package.
 * <p>
 * Instances of these interfaces have undefined lifecycles, and should never be retained.
 * <p>
 * Currently, this family of interfaces is effectively an implementation detail.
 * In the future, they will be given functionality on par with their MixinExtras counterparts.
 */
public sealed interface ObjectRef<T> permits ObjectRefImpl {
	T get();

	void set(T value);
}
