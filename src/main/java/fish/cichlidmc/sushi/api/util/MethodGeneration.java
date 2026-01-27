package fish.cichlidmc.sushi.api.util;

import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.registry.Id;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utilities for adding new methods to classes.
 */
public final class MethodGeneration {
	public static final Set<AccessFlag> STATIC_LAMBDA_FLAGS = flagSet(AccessFlag.PRIVATE, AccessFlag.STATIC, AccessFlag.SYNTHETIC);

	private MethodGeneration() {}

	public static void generate(ClassBuilder builder, String name, MethodTypeDesc desc, Set<AccessFlag> flags, Consumer<MethodBuilder> consumer) {
		int flagsMask = toMask(flags);
		builder.withMethod(name, desc, flagsMask, consumer);
	}

	public static String createUniqueName(Set<MethodKey> methods, String prefix, Id owner) {
		String idealName = "sushi$" + prefix + '$' + owner.namespace + '$' + sanitizePath(owner.path);
		String name = idealName;

		Set<String> names = methods.stream().map(MethodKey::name).collect(Collectors.toSet());
		for (int i = 0; names.contains(name); i++) {
			name = idealName + '_' + i;
		}

		return name;
	}

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

	public static boolean isAllowedOnMethods(AccessFlag flag) {
		return flag.locations().contains(AccessFlag.Location.METHOD);
	}

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

	private static String sanitizePath(String path) {
		return path.replace('.', '_').replace('/', '_');
	}
}
