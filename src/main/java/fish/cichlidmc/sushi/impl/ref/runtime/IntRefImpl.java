package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.IntRef;

public final class IntRefImpl extends BaseRefImpl implements IntRef {
	private int value;

	public IntRefImpl() {
		this(0);
	}

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

	public static void set(int value, IntRefImpl ref) {
		ref.set(value);
	}
}
