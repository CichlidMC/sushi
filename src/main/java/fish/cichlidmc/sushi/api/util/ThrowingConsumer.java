package fish.cichlidmc.sushi.api.util;

import java.util.function.Consumer;

/// A [Consumer] that can throw exceptions.
@FunctionalInterface
public interface ThrowingConsumer<T, X extends Throwable> {
	void accept(T value) throws X;
}
