package fish.cichlidmc.sushi.api.util;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/// Utilities for adding new methods to classes.
public final class MethodGeneration {
	/// A set of flags that should be used for a static lambda.
	public static final Set<AccessFlag> STATIC_LAMBDA_FLAGS = flagSet(AccessFlag.PRIVATE, AccessFlag.STATIC, AccessFlag.SYNTHETIC);

	private MethodGeneration() {}

	/// Generate a new method.
	/// @param builder the [ClassBuilder] to add the method to
	/// @param consumer a consumer that will be invoked to build the method
	/// @see TransformableClass#createUniqueMethodName(String, Id)
	public static void generate(ClassBuilder builder, String name, MethodTypeDesc desc, Set<AccessFlag> flags, Consumer<MethodBuilder> consumer) {
		int flagsMask = toMask(flags);
		builder.withMethod(name, desc, flagsMask, consumer);
	}

	/// Create a new [Set] of [AccessFlag]s.
	/// @throws IllegalArgumentException if any flag is duplicated or not allowed on methods
	public static Set<AccessFlag> flagSet(AccessFlag... flags) {
		EnumSet<AccessFlag> set = EnumSet.noneOf(AccessFlag.class);

		for (AccessFlag flag : flags) {
			if (!isAllowedOnMethods(flag)) {
				throw new IllegalArgumentException("Invalid flag: " + flag);
			}

			if (!set.add(flag)) {
				throw new IllegalArgumentException("Duplicate flag: " + flag);
			}
		}

		return set;
	}

	/// @return true if the given flag can be placed on methods
	public static boolean isAllowedOnMethods(AccessFlag flag) {
		return flag.locations().contains(AccessFlag.Location.METHOD);
	}

	/// @return a bit mask representing the given set of flags
	/// @throws IllegalArgumentException if any flag is not allowed on methods
	public static int toMask(Set<AccessFlag> flags) {
		int mask = 0;

		for (AccessFlag flag : flags) {
			if (!isAllowedOnMethods(flag)) {
				throw new IllegalArgumentException("Flag not valid for methods: " + flag);
			}

			mask |= flag.mask();
		}

		return mask;
	}
}
