package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.FloatRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class FloatRefImpl extends BaseRefImpl implements FloatRef {
	public static final ClassDesc API_DESC = ClassDescs.of(FloatRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(FloatRefImpl.class);

	private float value;

	public FloatRefImpl(float initial) {
		this.value = initial;
	}

	@Override
	public float get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(float value) {
		this.checkDiscarded();
		this.value = value;
	}
}
