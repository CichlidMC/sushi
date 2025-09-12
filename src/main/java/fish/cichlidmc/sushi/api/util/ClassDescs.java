package fish.cichlidmc.sushi.api.util;

import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilities for {@link ClassDesc}s.
 */
public final class ClassDescs {
	public static final Codec<ClassDesc> ANY_CODEC = Codec.STRING.comapFlatMap(ClassDescs::parse, ClassDescs::fullName);
	public static final Codec<ClassDesc> PRIMITIVE_CODEC = validated(ClassDesc::isPrimitive, "Not a primitive");
	public static final Codec<ClassDesc> CLASS_CODEC = validated(ClassDesc::isClassOrInterface, "Not a class");
	public static final Codec<ClassDesc> ARRAY_CODEC = validated(ClassDesc::isArray, "Not an array");

	private static final Map<String, ClassDesc> primitives = Arrays.stream(new ClassDesc[] {
			ConstantDescs.CD_int, ConstantDescs.CD_long, ConstantDescs.CD_float,
			ConstantDescs.CD_double, ConstantDescs.CD_short, ConstantDescs.CD_byte,
			ConstantDescs.CD_char, ConstantDescs.CD_boolean, ConstantDescs.CD_void
	}).collect(Collectors.toMap(ClassDesc::displayName, Function.identity()));

	private ClassDescs() {
	}

	public static ClassDesc of(Class<?> clazz) {
		return clazz.describeConstable().orElseThrow(() -> new IllegalArgumentException("Class cannot be described: " + clazz));
	}

	/**
	 * Get the boxed class (ex. {@link Boolean}) for a primitive class (ex. {@code boolean}).
	 * @throws IllegalArgumentException if the given {@link ClassDesc} is not a primitive
	 */
	public static ClassDesc box(ClassDesc primitive) {
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException("Not a primitive: " + primitive);
		}

		return switch (primitive.descriptorString()) {
			case "I" -> ConstantDescs.CD_Integer;
			case "J" -> ConstantDescs.CD_Long;
			case "F" -> ConstantDescs.CD_Float;
			case "D" -> ConstantDescs.CD_Double;
			case "S" -> ConstantDescs.CD_Short;
			case "B" -> ConstantDescs.CD_Byte;
			case "C" -> ConstantDescs.CD_Character;
			case "Z" -> ConstantDescs.CD_Boolean;
			case "V" -> ConstantDescs.CD_Void;
			default -> throw new IllegalArgumentException("Unknown primitive: " + primitive);
		};
	}

	/**
	 * Get the full name of a ClassDesc in binary form, ex. {@code int}, {@code java.lang.String}, or {@code java.lang.Object[]}
	 */
	public static String fullName(ClassDesc desc) {
		String name = desc.displayName();

		if (desc.isPrimitive())
			return name;

		String pkg = desc.isArray() ? arrayRoot(desc).packageName() : desc.packageName();
		if (pkg.isEmpty()) {
			// default package
			return name;
		}

		return pkg + '.' + name;
	}

	/**
	 * Get the lowest component of an array. Ex. {@code java.lang.Object[][] -> java.lang.Object}
	 */
	public static ClassDesc arrayRoot(ClassDesc desc) {
		if (!desc.isArray()) {
			throw new IllegalArgumentException("Not an array: " + desc);
		}

		while (desc.isArray()) {
			desc = desc.componentType();
		}

		return desc;
	}

	public static boolean equals(ClassDesc desc, Class<?> clazz) {
		return  clazz.describeConstable().map(other -> other.equals(desc)).orElse(false);
	}

	private static CodecResult<ClassDesc> parse(String string) {
		int dimensions = countDimensions(string);
		if (dimensions == -1) {
			return CodecResult.error("Malformed array dimensions: " + string);
		} else if (dimensions > 255) {
			return CodecResult.error("Too many array dimensions; " + dimensions + " > 255");
		}

		int bracket = string.indexOf('[');
		String withoutArrays = bracket == -1 ? string : string.substring(0, bracket);

		try {
			ClassDesc desc = parseNonArray(withoutArrays);

			if (dimensions > 0) {
				desc = desc.arrayType(dimensions);
			}

			return CodecResult.success(desc);
		} catch (IllegalArgumentException e) {
			return CodecResult.error("Failed to parse ClassDesc from '" + string + "': " + e.getMessage());
		}
	}

	private static int countDimensions(String string) {
		int dimensions = 0;

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (c == '[') {
				if (i + 1 == string.length()) {
					// trailing [, malformed
					return -1;
				}

				char next = string.charAt(i + 1);
				if (next == ']') {
					// pair complete
					dimensions++;
					// skip it in the next iteration
					i++;
				} else {
					// all opening brackets should be followed by a closing one, malformed
					return -1;
				}
			}
		}

		return dimensions;
	}

	private static ClassDesc parseNonArray(String string) throws IllegalArgumentException {
		if (primitives.containsKey(string)) {
			return primitives.get(string);
		}

		return ClassDesc.of(string);
	}

	private static Codec<ClassDesc> validated(Predicate<ClassDesc> test, String errorDescription) {
		return ANY_CODEC.validate(
				desc -> test.test(desc) ? CodecResult.success(desc) : CodecResult.error(errorDescription + ": " + fullName(desc))
		);
	}
}
