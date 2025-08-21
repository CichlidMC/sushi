package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.ShortRef;

public final class ShortRefImpl extends RefImplBase implements ShortRef {
	private short value;

	public ShortRefImpl(short initial) {
		this.value = initial;
	}

	@Override
	public short get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(short value) {
		this.checkDiscarded();
		this.value = value;
	}
}
