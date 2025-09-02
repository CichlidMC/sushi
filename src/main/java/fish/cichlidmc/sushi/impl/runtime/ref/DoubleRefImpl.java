package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.DoubleRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class DoubleRefImpl extends BaseRefImpl implements DoubleRef {
	public static final ClassDesc API_DESC = ClassDescs.of(DoubleRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(DoubleRefImpl.class);

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
