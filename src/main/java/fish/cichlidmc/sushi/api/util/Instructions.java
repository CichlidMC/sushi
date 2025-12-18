package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.PseudoInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/// Utilities for handling [Instruction]s and [PseudoInstruction]s.
public final class Instructions {
	private Instructions() {
	}

	public static void assertInstruction(CodeElement element) {
		if (!isInstruction(element)) {
			throw new IllegalArgumentException("Not an Instruction or PseudoInstruction: " + element);
		}
	}

	public static boolean isInstruction(CodeElement element) {
		return element instanceof Instruction || element instanceof PseudoInstruction;
	}

	/// Push a [Opcode#CHECKCAST] only if the given type is non-primitive.
	public static void maybeCheckCast(CodeBuilder builder, ClassDesc expectedType) {
		if (!expectedType.isPrimitive()) {
			builder.checkcast(expectedType);
		}
	}

	public static void maybeUnbox(CodeBuilder builder, ClassDesc clazz) {
		if (!clazz.isPrimitive())
			return;

		ClassDesc boxed = ClassDescs.box(clazz);
		builder.checkcast(boxed);
		unbox(builder, clazz);
	}

	/// Generate an instruction to invoke the proper unboxing method for a boxed primitive.
	/// Examples: [Integer#intValue()], [Character#charValue()], [Short#shortValue()]
	/// @throws IllegalArgumentException if the given [ClassDesc] is a non-primitive or `void`
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
