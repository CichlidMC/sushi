package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.BoolRef;

public final class BoolRefImpl extends RefImplBase implements BoolRef {
	private boolean value;

	public BoolRefImpl(boolean initial) {
		this.value = initial;
	}

	@Override
	public boolean get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(boolean value) {
		this.checkDiscarded();
		this.value = value;
	}
}
