package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.ref.ByteRef;

public final class ByteRefImpl extends RefImplBase implements ByteRef {
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
