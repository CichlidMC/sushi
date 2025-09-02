package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.ObjectRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class ObjectRefImpl<T> extends BaseRefImpl implements ObjectRef<T> {
	public static final ClassDesc API_DESC = ClassDescs.of(ObjectRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(ObjectRefImpl.class);

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
