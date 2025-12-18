package fish.cichlidmc.sushi.impl.attach;

import fish.cichlidmc.sushi.api.attach.AttachmentKey;
import fish.cichlidmc.sushi.api.attach.AttachmentMap;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class AttachmentMapImpl implements AttachmentMap {
	private final Map<AttachmentKey<?>, Object> map;

	public AttachmentMapImpl() {
		this.map = new IdentityHashMap<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(AttachmentKey<T> key) {
		Object value = this.map.get(key);
		if (value == null) {
			return Optional.empty();
		}

		T t = (T) value;
		return Optional.of(t);
	}

	@Override
	public <T> void set(AttachmentKey<T> key, T value) {
		this.map.put(key, value);
	}

	@Override
	public boolean has(AttachmentKey<?> key) {
		return this.map.containsKey(key);
	}

	@Override
	public <T> T getOrCreate(AttachmentKey<T> key, Supplier<T> factory) {
		Optional<T> value = this.get(key);
		if (value.isPresent()) {
			return value.get();
		}

		T created = factory.get();
		this.set(key, created);
		return created;
	}
}
