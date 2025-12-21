package fish.cichlidmc.sushi.api.util;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.PseudoInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

/// Utilities for handling [Instruction]s and [PseudoInstruction]s.
public final class Instructions {
	private Instructions() {
	}

	/// Push an invoke instruction that invokes the given method.
	/// @param handle a [DirectMethodHandleDesc] pointing to the method to invoke
	public static void invokeMethod(CodeBuilder builder, DirectMethodHandleDesc handle) {
		Opcode opcode = switch (handle.kind()) {
			case STATIC, INTERFACE_STATIC -> Opcode.INVOKESTATIC;
			case VIRTUAL -> Opcode.INVOKEVIRTUAL;
			case INTERFACE_VIRTUAL -> Opcode.INVOKEINTERFACE;
			case SPECIAL, INTERFACE_SPECIAL -> Opcode.INVOKESPECIAL;
			default -> throw new IllegalArgumentException("Not a method: " + handle);
		};

		builder.invoke(opcode, handle.owner(), handle.methodName(), handle.invocationType(), handle.isOwnerInterface());
	}

	/// Push a [Opcode#CHECKCAST] only if the given type is non-primitive.
	public static void maybeCheckCast(CodeBuilder builder, ClassDesc expectedType) {
		if (!expectedType.isPrimitive()) {
			builder.checkcast(expectedType);
		}
	}

	/// Defers to [#unboxChecked(CodeBuilder, ClassDesc)], but only if the given [ClassDesc] represents a primitive type.
	public static void maybeUnbox(CodeBuilder builder, ClassDesc clazz) {
		if (clazz.isPrimitive()) {
			unboxChecked(builder, clazz);
		}
	}

	/// Unbox a primitive wrapper after checking that the proper box type is present.
	///
	/// Expects the top of the stack to hold the boxed value.
	/// After invocation, the box will be replaced with the unboxed value.
	/// @throws IllegalArgumentException if `primitive` is not actually a primitive type
	public static void unboxChecked(CodeBuilder builder, ClassDesc primitive) {
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException("Not a primitive: " + primitive);
		}

		ClassDesc boxed = ClassDescs.box(primitive);
		builder.checkcast(boxed);
		unbox(builder, primitive);
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
