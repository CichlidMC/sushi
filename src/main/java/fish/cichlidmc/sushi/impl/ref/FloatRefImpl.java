package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.FloatRef;

public final class FloatRefImpl extends RefImplBase implements FloatRef {
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
