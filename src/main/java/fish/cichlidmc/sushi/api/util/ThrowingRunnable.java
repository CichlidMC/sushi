package fish.cichlidmc.sushi.api.util;

/// A [Runnable] that can throw exceptions.
@FunctionalInterface
public interface ThrowingRunnable<X extends Throwable> {
	void run() throws X;
}
