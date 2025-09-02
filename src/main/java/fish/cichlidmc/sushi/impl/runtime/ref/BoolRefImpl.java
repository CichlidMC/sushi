package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.BoolRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class BoolRefImpl extends BaseRefImpl implements BoolRef {
	public static final ClassDesc API_DESC = ClassDescs.of(BoolRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(BoolRefImpl.class);

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
