package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.IntRef;

public final class IntRefImpl extends RefImplBase implements IntRef {
	private int value;

	public IntRefImpl(int initial) {
		this.value = initial;
	}

	@Override
	public int get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(int value) {
		this.checkDiscarded();
		this.value = value;
	}
}
