package fish.cichlidmc.sushi.api.util;

import java.util.function.Supplier;

/// A [Supplier] that can throw exceptions.
@FunctionalInterface
public interface ThrowingSupplier<T, X extends Throwable> {
	T get() throws X;
}
