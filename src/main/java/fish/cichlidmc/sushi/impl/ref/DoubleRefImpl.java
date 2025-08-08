package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.DoubleRef;

public final class DoubleRefImpl extends RefImplBase implements DoubleRef {
	private double value;

	public DoubleRefImpl(double initial) {
		this.value = initial;
	}

	@Override
	public double get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(double value) {
		this.checkDiscarded();
		this.value = value;
	}
}
