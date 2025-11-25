package fish.cichlidmc.sushi.api.transformer.infra;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents the cancellation of some operation, typically the target method of an inject.
 * <p>
 * Null is used to represent no cancellation. When non-null, {@link #value} holds the replacement return value.
 */
public final class Cancellation<T> {
	// cache and reuse a cancellation that indicates cancelling and returning null.
	// note: does not use of(null), since that would be circular
	private static final Cancellation<?> nullResult = new Cancellation<>(null);

	@Nullable
	public final T value;

	private Cancellation(@Nullable T value) {
		this.value = value;
	}

	/**
	 * Indicates that a cancellation did not occur.
	 * This is just a fancy way to return null really, but indicates intent.
	 */
	@Nullable
	public static <T> Cancellation<T> none() {
		return null;
	}

	/**
	 * Create a new Cancellation holding the given nullable value.
	 */
	public static <T> Cancellation<T> of(@Nullable T value) {
		return value == null ? castNullResult() : new Cancellation<>(value);
	}


	/**
	 * If the given value is non-null, returns a new Cancellation. Otherwise, returns null.
	 * This can be used to easily cancel a method with an optionally overridden value.
	 */
	@Nullable
	public static <T> Cancellation<T> ifPresent(@Nullable T value) {
		return value == null ? none() : of(value);
	}

	/**
	 * Same as {@link #ifPresent(Object)}, but takes an optional instead of a nullable value.
	 */
	@Nullable
	public static <T> Cancellation<T> ifPresent(Optional<T> value) {
		return ifPresent(value.orElse(null));
	}

	/**
	 * Shortcut for cancelling a void method.
	 */
	public static Cancellation<Void> ofVoid() {
		return of(null);
	}

	@SuppressWarnings("unchecked")
	private static <T> Cancellation<T> castNullResult() {
		return (Cancellation<T>) nullResult;
	}
}
