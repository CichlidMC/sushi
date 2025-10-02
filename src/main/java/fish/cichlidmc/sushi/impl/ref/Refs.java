package fish.cichlidmc.sushi.impl.ref;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.ref.BoolRef;
import fish.cichlidmc.sushi.api.ref.ByteRef;
import fish.cichlidmc.sushi.api.ref.CharRef;
import fish.cichlidmc.sushi.api.ref.DoubleRef;
import fish.cichlidmc.sushi.api.ref.FloatRef;
import fish.cichlidmc.sushi.api.ref.IntRef;
import fish.cichlidmc.sushi.api.ref.LongRef;
import fish.cichlidmc.sushi.api.ref.ObjectRef;
import fish.cichlidmc.sushi.api.ref.ShortRef;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.ref.runtime.BoolRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.ByteRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.CharRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.DoubleRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.FloatRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.IntRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.LongRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.ObjectRefImpl;
import fish.cichlidmc.sushi.impl.ref.runtime.ShortRefImpl;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

public final class Refs {
	// methods invoked at runtime
	public static final MethodTypeDesc DISCARD_DESC = MethodTypeDesc.of(ConstantDescs.CD_void);
	public static final MethodTypeDesc PARAMETERLESS_CONSTRUCTOR_DESC = MethodTypeDesc.of(ConstantDescs.CD_void);

	private Refs() {}

	public static Type holderOf(ClassDesc type) {
		if (!type.isPrimitive())
			return Type.OBJECT;

		return switch (type.descriptorString()) {
			case "I" -> Type.INT;
			case "J" -> Type.LONG;
			case "F" -> Type.FLOAT;
			case "D" -> Type.DOUBLE;
			case "S" -> Type.SHORT;
			case "B" -> Type.BYTE;
			case "C" -> Type.CHAR;
			case "Z" -> Type.BOOL;
			case "V" -> throw new IllegalArgumentException("no refs for void");
			default -> throw new IllegalArgumentException("Unknown primitive: " + type);
		};
	}

	public enum Type {
		BOOL(boolean.class, BoolRef.class, BoolRefImpl.class),
		BYTE(byte.class, ByteRef.class, ByteRefImpl.class),
		CHAR(char.class, CharRef.class, CharRefImpl.class),
		DOUBLE(double.class, DoubleRef.class, DoubleRefImpl.class),
		FLOAT(float.class, FloatRef.class, FloatRefImpl.class),
		INT(int.class, IntRef.class, IntRefImpl.class),
		LONG(long.class, LongRef.class, LongRefImpl.class),
		OBJECT(Object.class, ObjectRef.class, ObjectRefImpl.class),
		SHORT(short.class, ShortRef.class, ShortRefImpl.class);

		public final ClassDesc held;
		public final ClassDesc api;
		public final ClassDesc impl;

		private final MethodTypeDesc parameterizedConstructorDesc;
		private final MethodTypeDesc getDesc;

		Type(Class<?> heldClass, Class<?> apiClass, Class<?> implClass) {
			this.held = ClassDescs.of(heldClass);
			this.api = ClassDescs.of(apiClass);
			this.impl = ClassDescs.of(implClass);


			this.parameterizedConstructorDesc = PARAMETERLESS_CONSTRUCTOR_DESC.insertParameterTypes(0, this.held);
			this.getDesc = MethodTypeDesc.of(this.held);
		}

		public void constructParameterless(CodeBuilder builder) {
			this.pushNew(builder);
			builder.invokespecial(this.impl, "<init>", PARAMETERLESS_CONSTRUCTOR_DESC);
		}

		public void constructParameterized(CodeBuilder builder, CodeBlock parameterProvider) {
			this.pushNew(builder);
			parameterProvider.write(builder);
			builder.invokespecial(this.impl, "<init>", this.parameterizedConstructorDesc);
		}

		public void invokeGet(CodeBuilder builder) {
			builder.invokevirtual(this.impl, "get", this.getDesc);
		}

		public void invokeDiscard(CodeBuilder builder) {
			builder.invokevirtual(this.impl, "discard", DISCARD_DESC);
		}

		private void pushNew(CodeBuilder builder) {
			builder.new_(this.impl);
			builder.dup();
		}
	}
}
