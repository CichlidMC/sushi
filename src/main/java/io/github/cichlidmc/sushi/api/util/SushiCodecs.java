package io.github.cichlidmc.sushi.api.util;

import io.github.cichlidmc.sushi.impl.util.TypeCodec;
import io.github.cichlidmc.tinycodecs.Codec;
import org.objectweb.asm.Type;

public final class SushiCodecs {
	public static final Codec<Type> TYPE = TypeCodec.INSTANCE;
}
