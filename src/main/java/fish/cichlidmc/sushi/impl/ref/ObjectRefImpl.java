package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.ObjectRef;

public final class ObjectRefImpl<T> extends RefImplBase implements ObjectRef<T> {
	private T value;

	public ObjectRefImpl(T initial) {
		this.value = initial;
	}

	@Override
	public T get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(T value) {
		this.checkDiscarded();
		this.value = value;
	}
}
