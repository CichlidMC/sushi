package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.ObjectRef;
import org.jspecify.annotations.Nullable;

public final class ObjectRefImpl<T> extends BaseRefImpl implements ObjectRef<T> {
	@Nullable
	private T value;

	public ObjectRefImpl() {
		this(null);
	}

	public ObjectRefImpl(@Nullable T initial) {
		this.value = initial;
	}

	@Nullable
	@Override
	public T get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(@Nullable T value) {
		this.checkDiscarded();
		this.value = value;
	}

	public static <T> void set(@Nullable T value, ObjectRefImpl<T> ref) {
		ref.set(value);
	}
}
