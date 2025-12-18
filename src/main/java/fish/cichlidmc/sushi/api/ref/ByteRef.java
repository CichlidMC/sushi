package fish.cichlidmc.sushi.api.ref;

import fish.cichlidmc.sushi.impl.ref.runtime.ByteRefImpl;

/// Primitive `byte`-specialized variant of [ObjectRef].
public sealed interface ByteRef permits ByteRefImpl {
	byte get();

	void set(byte value);
}
