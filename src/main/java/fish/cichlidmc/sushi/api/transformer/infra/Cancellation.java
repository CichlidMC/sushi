package fish.cichlidmc.sushi.api.transformer.infra;

import org.jspecify.annotations.Nullable;

/// Represents the cancellation of some operation, typically the target method of an inject.
///
/// Null is used to represent no cancellation. When non-null, [#value] holds the replacement return value.
public final class Cancellation<T extends @Nullable Object> {
	// cache and reuse a cancellation that indicates cancelling and returning null.
	// note: does not use of(null), since that would be circular
	private static final Cancellation<?> nullResult = new Cancellation<>(null);

	public final T value;

	private Cancellation(T value) {
		this.value = value;
	}

	/// Indicates that a cancellation did not occur.
	/// This is just a fancy way to return null really, but indicates intent.
	@Nullable
	public static <T extends @Nullable Object> Cancellation<T> none() {
		return null;
	}

	/// Create a new Cancellation holding the given nullable value.
	public static <T extends @Nullable Object> Cancellation<T> of(T value) {
		return value == null ? castNullResult() : new Cancellation<>(value);
	}

	/// If the given value is non-null, returns a new Cancellation. Otherwise, returns null.
	/// This can be used to easily cancel a method with an optionally overridden value.
	@Nullable
	public static <T> Cancellation<T> ifPresent(@Nullable T value) {
		return value == null ? none() : of(value);
	}

	/// Shortcut for cancelling a void method.
	public static Cancellation<@Nullable Void> ofVoid() {
		return of(null);
	}

	@SuppressWarnings("unchecked")
	private static <T> Cancellation<T> castNullResult() {
		return (Cancellation<T>) nullResult;
	}
}
