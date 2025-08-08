package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.CharRef;

public final class CharRefImpl extends RefImplBase implements CharRef {
	private char value;

	public CharRefImpl(char initial) {
		this.value = initial;
	}

	@Override
	public char get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(char value) {
		this.checkDiscarded();
		this.value = value;
	}
}
