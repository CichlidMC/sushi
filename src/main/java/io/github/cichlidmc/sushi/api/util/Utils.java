package io.github.cichlidmc.sushi.api.util;

import org.objectweb.asm.Type;

import java.util.Arrays;

public final class Utils {
	private Utils() {}

	public static String createMethodDesc(Class<?> returnType, Class<?>... parameters) {
		Type returnTypeType = Type.getType(returnType);
		Type[] parameterTypes = Arrays.stream(parameters)
				.map(Type::getType)
				.toArray(Type[]::new);
		return Type.getMethodDescriptor(returnTypeType, parameterTypes);
	}
}
