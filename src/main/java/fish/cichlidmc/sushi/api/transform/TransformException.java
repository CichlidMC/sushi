package fish.cichlidmc.sushi.api.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An exception that occurred while transforming a class.
 * May be thrown by transformers if an irrecoverable problem occurs.
 * <p>
 * Like all Exceptions, a TransformException has a message.
 * However, unlike most other Exceptions, the message may not be null.
 * <p>
 * In addition, a TransformException contains a list of additional details.
 * A TransformException may be caught and re-thrown many times, with each
 * catch adding more relevant information.
 * <p>
 * It's possible to manually call {@link #addDetail(String, Object)}, but it's
 * recommended to instead use {@link #withDetail(String, Object, Runnable)} or an overload.
 */
public class TransformException extends RuntimeException {
	private static final String hiddenMessage = "This should never be visible! Please report this.";

	private final String message;
	private final List<Detail> details;

	public TransformException(String message) {
		this(message, null);
	}

	public TransformException(String message, Throwable cause) {
		super(hiddenMessage, cause);
		this.message = Objects.requireNonNull(message);
		this.details = new ArrayList<>();
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder(this.message)
				.append("\nDetails:");

		if (this.details.isEmpty()) {
			return builder.append(" <none>").toString();
		}

		// iterate end-to-start, since details are added bottom-to-top
		for (Detail detail : this.details.reversed()) {
			builder.append("\n\t- ").append(detail.name).append(": ").append(detail.value);
		}

		return builder.toString();
	}

	/**
	 * Add a new detail to this exception.
	 * @param name the name of the detail. Human-readable, non-unique.
	 * @param value an arbitrary value. {@link Object#toString()} will be invoked on it.
	 */
	public void addDetail(String name, Object value) {
		this.details.add(new Detail(name, value.toString()));
	}

	/**
	 * Execute the given runnable. If a TransformException is thrown, the given detail
	 * will be added to it. If a different exception occurs, it will be wrapped in a
	 * TransformException, and then that will get the given detail added to it.
	 */
	public static void withDetail(String name, Object value, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			throw handleCatch(t, e -> e.addDetail(name, value));
		}
	}

	/**
	 * Identical to {@link #withDetail(String, Object, Runnable)}, but receives a supplier
	 * instead of a runnable. If no exception occurs, the result of the supplier will be returned.
	 */
	public static <T> T withDetail(String name, Object value, Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			throw handleCatch(t, e -> e.addDetail(name, value));
		}
	}

	/**
	 * Identical to {@link #withDetail(String, Object, Runnable)}, but can provide
	 * multiple details via the given {@link DetailProvider}.
	 */
	public static void withDetails(DetailProvider provider, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			throw handleCatch(t, provider::addDetails);
		}
	}

	/**
	 * Identical to {@link #withDetail(String, Object, Supplier)}, but can provide
	 * multiple details via the given {@link DetailProvider}.
	 */
	public static <T> T withDetails(DetailProvider provider, Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			throw handleCatch(t, provider::addDetails);
		}
	}

	public static TransformException of(String message, DetailProvider details) {
		TransformException e = new TransformException(message);
		details.addDetails(e);
		return e;
	}

	public static TransformException of(String message, Throwable cause, DetailProvider details) {
		TransformException e = new TransformException(message, cause);
		details.addDetails(e);
		return e;
	}

	private static TransformException handleCatch(Throwable t, Consumer<TransformException> consumer) {
		if (t instanceof TransformException exception) {
			consumer.accept(exception);
			return exception;
		} else {
			TransformException exception = new TransformException("Unhandled exception", t);
			consumer.accept(exception);
			return exception;
		}
	}

	public interface DetailProvider {
		void addDetails(TransformException exception);
	}

	private record Detail(String name, String value) {
	}
}
