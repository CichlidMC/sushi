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

	/**
	 * Get an existing attachment, or create it from the given supplier.
	 * If a new instance is created, it stored in this map and then returned.
	 */
	<T> T getOrCreate(AttachmentKey<T> key, Supplier<T> factory);

	/**
	 * @return a new, empty map
	 */
	static AttachmentMap create() {
		return new AttachmentMapImpl();
	}
}
