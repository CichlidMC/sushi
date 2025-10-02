package fish.cichlidmc.sushi.impl.ref.runtime;

import fish.cichlidmc.sushi.api.ref.ByteRef;

public final class ByteRefImpl extends BaseRefImpl implements ByteRef {
	private byte value;

	public ByteRefImpl() {
		this((byte) 0);
	}

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
