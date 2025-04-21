package io.github.cichlidmc.sushi.api.util;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Utils {
	private Utils() {}

	public static String createMethodDesc(Class<?> returnType, Class<?>... parameters) {
		Type returnTypeType = Type.getType(returnType);
		Type[] parameterTypes = Arrays.stream(parameters)
				.map(Type::getType)
				.toArray(Type[]::new);
		return Type.getMethodDescriptor(returnTypeType, parameterTypes);
	}

	/**
	 * Merges two lists into one, sorted by natural order.
	 */
	public static <T extends Comparable<T>> List<T> merge(@Nullable List<T> first, @Nullable List<T> second) {
		if (first == null || first.isEmpty()) {
			return second == null ? Collections.emptyList() : second;
		} else if (second == null || second.isEmpty()) {
			return first;
		} else {
			List<T> merged = new ArrayList<>(first);
			merged.addAll(second);
			merged.sort(Comparator.naturalOrder());
			return merged;
		}
	}
}
