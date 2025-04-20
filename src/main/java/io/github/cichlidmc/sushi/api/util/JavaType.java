package io.github.cichlidmc.sushi.api.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.CodecResult;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

/**
 * Represents a type in Java. May be a class/interface, primitive, or array.
 */
public final class JavaType {
	private static final char[] identifierBlacklist = { '.', ';', '[', '/' };

	private static final NameMapper<JavaType> primitives = new NameMapper<>();

	public static final JavaType VOID = primitive(Type.VOID_TYPE);
	public static final JavaType BOOL = primitive(Type.BOOLEAN_TYPE);
	public static final JavaType BYTE = primitive(Type.BYTE_TYPE);
	public static final JavaType SHORT = primitive(Type.SHORT_TYPE);
	public static final JavaType CHAR = primitive(Type.CHAR_TYPE);
	public static final JavaType INT = primitive(Type.INT_TYPE);
	public static final JavaType LONG = primitive(Type.LONG_TYPE);
	public static final JavaType FLOAT = primitive(Type.FLOAT_TYPE);
	public static final JavaType DOUBLE = primitive(Type.DOUBLE_TYPE);

	// codecs for pretty formats. "boolean", "java.lang.Object", "int[][]"
	public static final Codec<JavaType> PRIMITIVE_CODEC = primitives.codec;
	public static final Codec<JavaType> CLASS_CODEC = Codec.STRING.validate(JavaType::validateClassName).xmap(JavaType::new, type -> type.name);
	public static final Codec<JavaType> ARRAY_CODEC = Codec.STRING.validate(JavaType::validateArrayName).xmap(JavaType::new, type -> type.name);
	public static final Codec<JavaType> CODEC = PRIMITIVE_CODEC.withAlternative(ARRAY_CODEC.withAlternative(CLASS_CODEC));

	public final Type asmType;
	/**
	 * String representation of this type.
	 * For a primitive, this is its descriptor character, such as "I" or "J".
	 * For a class, this is its name, such as "java.lang.Object".
	 * For an array, this is its entry type's name, preceded by one open bracket for each dimension, such as "[java.lang.Object".
	 */
	public final String name;

	/**
	 * This type's name, but with all dots replaced with slashes.
	 */
	public final String internalName;

	private JavaType(Type asmType) {
		this.asmType = asmType;
		this.internalName = asmType.getInternalName();
		this.name = this.internalName.replace('/', '.');
	}

	private JavaType(String name) {
		this(Type.getObjectType(name.replace('.', '/')));
	}

	public boolean isPrimitive() {
		return primitives.contains(this);
	}

	/**
	 * @return the {@link Opcodes opcode} used to return this type from a method
	 */
	public int returnCode() {
		if (this == BOOL || this == BYTE || this == SHORT || this == CHAR || this == INT) {
			return Opcodes.IRETURN;
		} else if (this == LONG) {
			return Opcodes.LRETURN;
		} else if (this == FLOAT) {
			return Opcodes.FRETURN;
		} else if (this == DOUBLE) {
			return Opcodes.DRETURN;
		} else if (this == VOID) {
			return Opcodes.RETURN;
		} else {
			return Opcodes.RETURN;
		}
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == JavaType.class && this.name.equals(((JavaType) obj).name);
	}

	@Override
	public String toString() {
		return this.name;
	}

	public boolean matches(Type type) {
		return type.getInternalName().equals(this.name);
	}

	public boolean matches(ClassNode clazz) {
		return this.internalName.equals(clazz.name);
	}

	private static JavaType primitive(Type asmType) {
		JavaType type = new JavaType(asmType);
		primitives.put(asmType.getClassName(), type);
		return type;
	}

	public static JavaType of(Type asmType) {
		String name = asmType.getClassName();
		if (primitives.containsName(name))
			return primitives.get(name);

		return new JavaType(asmType);
	}

	public static JavaType of(Class<?> clazz) {
		return new JavaType(Type.getType(clazz));
	}

	public static String methodDesc(JavaType returnType, JavaType... parameters) {
		Type[] parametersAsm = Arrays.stream(parameters)
						.map(type -> type.asmType)
								.toArray(Type[]::new);
		return Type.getMethodDescriptor(returnType.asmType, parametersAsm);
	}

	/**
	 * Parse a class type from the given string, returning null if invalid or not a class.
	 */
	@Nullable
	public static JavaType parseClass(String string) {
		return validateClassName(string).map(JavaType::new).asOptional().orElse(null);
	}

	private static CodecResult<String> validateArrayName(String name) {
		int dimensions = 0;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '[') {
				dimensions++;
			} else {
				break;
			}
		}

		if (dimensions == 0) {
			return CodecResult.error("Not an array: " + name);
		} else {
			return validateClassName(name.substring(dimensions)).map($ -> name);
		}
	}

	private static CodecResult<String> validateClassName(String name) {
		if (primitives.containsName(name)) {
			return CodecResult.error("Not a class: " + name);
		}

		// https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.2.1
		String[] identifiers = name.split("\\.");
		for (String identifier : identifiers) {
			if (!isValidUnqualifiedName(identifier)) {
				return CodecResult.error("Invalid identifier in name: " + name);
			}
		}

		return CodecResult.success(name);
	}

	private static boolean isValidUnqualifiedName(String string) {
		// https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.2.2
		if (string.isEmpty())
			return false;

		for (char c : identifierBlacklist) {
			if (string.indexOf(c) != -1) {
				return false;
			}
		}

		return true;
	}
}
