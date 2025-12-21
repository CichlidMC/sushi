package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.CharRef;

public final class CharRefImpl extends BaseRefImpl implements CharRef {
	private char value;

	public CharRefImpl() {
		this((char) 0);
	}

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

	public static void set(char value, CharRefImpl ref) {
		ref.set(value);
	}
}
