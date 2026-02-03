package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.ObjectRefImpl;
import org.jspecify.annotations.Nullable;

/// A mutable reference to an object.
///
/// Also comes in primitive-specialized variants; see package.
///
/// Instances of these interfaces have undefined lifecycles, and should never be retained.
public sealed interface ObjectRef<T extends @Nullable Object> permits ObjectRefImpl {
	T get();

	void set(T value);
}
