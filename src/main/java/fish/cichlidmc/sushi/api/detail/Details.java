package fish.cichlidmc.sushi.api.detail;

import fish.cichlidmc.sushi.api.detail.DetailedException.Factory;
import fish.cichlidmc.sushi.api.util.ThrowingRunnable;
import fish.cichlidmc.sushi.api.util.ThrowingSupplier;
import fish.cichlidmc.sushi.impl.detail.DetailsImpl;

import java.util.function.Consumer;

/// A set of [Detail]s belonging to a [DetailedException].
public sealed interface Details extends Iterable<Detail> permits DetailsImpl {
	/// Add a new detail directly.
	///
	/// [Object#toString()] will be invoked on the given object. If doing so throws
	/// an exception, the exception message will become the detail value.
	void add(String name, Object value);

	/// Execute the given runnable. If an exception occurs, the
	/// given detail will be added to it, and it will be rethrown.
	/// @param factory a factory for a new [DetailedException] for wrapping detail-less exceptions
	/// @param runnable the runnable to run
	static <X extends Throwable & DetailedException> void with(String name, Object value, Factory<X> factory, ThrowingRunnable<X> runnable) throws X {
		try {
			runnable.run();
		} catch (Throwable caught) {
			throw handleCatch(caught, factory, details -> details.add(name, value));
		}
	}

	/// Identical to [#with(String, Object, Factory, ThrowingRunnable)], but executes a supplier
	/// instead of a runnable. If no exception occurs, the result of the supplier will be returned.
	static <T, X extends Throwable & DetailedException> T with(String name, Object value, Factory<X> factory, ThrowingSupplier<T, X> supplier) throws X {
		try {
			return supplier.get();
		} catch (Throwable caught) {
			throw handleCatch(caught, factory, details -> details.add(name, value));
		}
	}

	@SuppressWarnings("unchecked")
	private static <X extends Throwable & DetailedException> X handleCatch(Throwable caught, Factory<X> factory, Consumer<Details> consumer) {
		if (caught instanceof DetailedException detailed) {
			consumer.accept(detailed.details());
			return (X) detailed;
		} else {
			X exception = factory.create("Uncaught Exception", caught);
			consumer.accept(exception.details());
			return exception;
		}
	}
}
