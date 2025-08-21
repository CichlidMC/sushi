package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.LongRef;

public final class LongRefImpl extends RefImplBase implements LongRef {
	private long value;

	public LongRefImpl(long initial) {
		this.value = initial;
	}

	@Override
	public long get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(long value) {
		this.checkDiscarded();
		this.value = value;
	}
}
