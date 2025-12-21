package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.FloatRef;

public final class FloatRefImpl extends BaseRefImpl implements FloatRef {
	private float value;

	public FloatRefImpl() {
		this(0);
	}

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

	public static void set(float value, FloatRefImpl ref) {
		ref.set(value);
	}
}
