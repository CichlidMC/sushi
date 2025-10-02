package fish.cichlidmc.sushi.api.attach;

import fish.cichlidmc.sushi.impl.attach.AttachmentMapImpl;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A map holding arbitrary additional data that is attached to an object.
 */
public sealed interface AttachmentMap permits AttachmentMapImpl {
	<T> Optional<T> get(AttachmentKey<T> key);

	<T> void set(AttachmentKey<T> key, T value);

	boolean has(AttachmentKey<?> key);

	<T> T getOrCreate(AttachmentKey<T> key, Supplier<T> factory);
}
