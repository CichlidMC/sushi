package fish.cichlidmc.sushi.api.transformer.infra;

/**
 * Represents an arbitrary wrapped operation. This should look familiar if you've ever used Mixin Extras.
 * @param <T> the type returned by invoking the operation
 */
public interface Operation<T> {
	T call(Object... args);
}
