package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;

import java.util.List;

public final class MethodDesc {
	public static final Codec<MethodDesc> CODEC = CompositeCodec.of(
			Codecs.STRING.validate(MethodDesc::isValidMethodName).fieldOf("name"), method -> method.name,
			JavaType.CODEC.listOf().fieldOf("parameters"), method -> method.parameters,
			JavaType.CODEC.fieldOf("return"), method -> method.returnType,
			MethodDesc::new
	).asCodec();

	public final String name;
	public final List<JavaType> parameters;
	public final JavaType returnType;

	private MethodDesc(String name, List<JavaType> parameters, JavaType returnType) {
		this.name = name;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	public static boolean isValidMethodName(String name) {
		// TODO
		return true;
	}
}
