package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.ShortRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class ShortRefImpl extends BaseRefImpl implements ShortRef {
	public static final ClassDesc API_DESC = ClassDescs.of(ShortRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(ShortRefImpl.class);

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
