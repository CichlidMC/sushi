package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.CharRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class CharRefImpl extends BaseRefImpl implements CharRef {
	public static final ClassDesc API_DESC = ClassDescs.of(CharRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(CharRefImpl.class);

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
