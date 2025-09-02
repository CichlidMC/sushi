package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.IntRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class IntRefImpl extends BaseRefImpl implements IntRef {
	public static final ClassDesc API_DESC = ClassDescs.of(IntRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(IntRefImpl.class);

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
