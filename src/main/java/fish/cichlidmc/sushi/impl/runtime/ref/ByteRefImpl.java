package fish.cichlidmc.sushi.impl.runtime.ref;

import fish.cichlidmc.sushi.api.ref.ByteRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

public final class ByteRefImpl extends BaseRefImpl implements ByteRef {
	public static final ClassDesc API_DESC = ClassDescs.of(ByteRef.class);
	public static final ClassDesc IMPL_DESC = ClassDescs.of(ByteRefImpl.class);

	private byte value;

	public ByteRefImpl(byte initial) {
		this.value = initial;
	}

	@Override
	public byte get() {
		this.checkDiscarded();
		return this.value;
	}

	@Override
	public void set(byte value) {
		this.checkDiscarded();
		this.value = value;
	}
}
