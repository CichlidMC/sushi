package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 * Utilities for handling {@link Instruction}s and {@link PseudoInstruction}s.
 */
public final class Instructions {
	private Instructions() {}

	public static void assertInstruction(CodeElement element) {
		if (!isInstruction(element)) {
			throw new IllegalArgumentException("Not an Instruction or PseudoInstruction: " + element);
		}
	}

	public static boolean isInstruction(CodeElement element) {
		return element instanceof Instruction || element instanceof PseudoInstruction;
	}

	public static void maybeUnbox(CodeBuilder builder, ClassDesc clazz) {
		if (!clazz.isPrimitive())
			return;

		ClassDesc boxed = ClassDescs.box(clazz);
		builder.checkcast(boxed);
		unbox(builder, clazz);
	}

	/**
	 * Generate an instruction to invoke the proper unboxing method for a boxed primitive.
	 * Examples: {@link Integer#intValue()}, {@link Character#charValue()}, {@link Short#shortValue()}
	 * @throws IllegalArgumentException if the given {@link ClassDesc} is a non-primitive or {@code void}
	 */
	public static void unbox(CodeBuilder builder, ClassDesc primitive) {
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException("Not a primitive: " + primitive);
		} else switch (primitive.descriptorString()) {
			case "I" -> unbox(builder, Integer.class, primitive, "intValue");
			case "J" -> unbox(builder, Long.class, primitive, "longValue");
			case "F" -> unbox(builder, Float.class, primitive, "floatValue");
			case "D" -> unbox(builder, Double.class, primitive, "doubleValue");
			case "S" -> unbox(builder, Short.class, primitive, "shortValue");
			case "B" -> unbox(builder, Byte.class, primitive, "byteValue");
			case "C" -> unbox(builder, Character.class, primitive, "charValue");
			case "Z" -> unbox(builder, Boolean.class, primitive, "booleanValue");
			case "V" -> throw new IllegalArgumentException("Cannot unbox void");
			default -> throw new IllegalArgumentException("Unknown primitive: " + primitive);
		}
	}

	private static void unbox(CodeBuilder builder, Class<?> boxed, ClassDesc primitive, String name) {
		ClassDesc owner = ClassDescs.of(boxed);
		MethodTypeDesc desc = MethodTypeDesc.of(primitive);
		builder.invokevirtual(owner, name, desc);
	}
}
