package fish.cichlidmc.sushi.api.transformer.infra;

import org.jspecify.annotations.Nullable;

/// Represents an arbitrary wrapped operation. This should look familiar if you've ever used Mixin Extras.
/// @param <T> the type returned by invoking the operation
public interface Operation<T extends @Nullable Object> {
	T call(@Nullable Object... args);
}
