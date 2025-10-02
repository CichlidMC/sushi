package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.ByteRefImpl;

/**
 * Primitive {@code byte}-specialized variant of {@link ObjectRef}.
 */
public sealed interface ByteRef permits ByteRefImpl {
	byte get();

	void set(byte value);
}
