package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.LongRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class LongRefImpl extends BaseRefImpl implements LongRef {
	public static final ClassDesc API_DESC = ClassDescs.of(LongRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(LongRefImpl.class);

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
