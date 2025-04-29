package fish.cichlidmc.sushi.api.transform.wrap_op;

/**
 * Represents an operation wrapped by a {@code wrap_operation} transform.
 * @param <T> the type returned by invoking the operation
 */
public interface Operation<T> {
	T call(Object... args);
}
