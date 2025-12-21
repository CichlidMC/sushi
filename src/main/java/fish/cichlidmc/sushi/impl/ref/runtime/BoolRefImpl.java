package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.BoolRef;

public final class BoolRefImpl extends BaseRefImpl implements BoolRef {
	private boolean value;

	public BoolRefImpl() {
		this(false);
	}

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

	public static void set(boolean value, BoolRefImpl ref) {
		ref.set(value);
	}
}
