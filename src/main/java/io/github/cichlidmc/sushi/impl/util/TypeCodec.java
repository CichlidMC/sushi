package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.DecodeResult;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum TypeCodec implements Codec<Type> {
	INSTANCE;

	private static final Map<String, Type> primitiveTypes = new HashMap<>();
	private static final Pattern dot = Pattern.compile(".", Pattern.LITERAL);

	static {
		primitiveTypes.put("void", Type.VOID_TYPE);
		primitiveTypes.put("boolean", Type.BOOLEAN_TYPE);
		primitiveTypes.put("byte", Type.BYTE_TYPE);
		primitiveTypes.put("short", Type.SHORT_TYPE);
		primitiveTypes.put("char", Type.CHAR_TYPE);
		primitiveTypes.put("int", Type.INT_TYPE);
		primitiveTypes.put("long", Type.LONG_TYPE);
		primitiveTypes.put("float", Type.FLOAT_TYPE);
		primitiveTypes.put("double", Type.DOUBLE_TYPE);
	}

	@Override
	public DecodeResult<Type> decode(JsonValue value) {
		if (!(value instanceof JsonString)) {
			return DecodeResult.error("Type must be a string");
		}

		String string = value.asString().value();

		Type primitive = primitiveTypes.get(string);
		if (primitive != null) {
			return DecodeResult.success(primitive);
		}

		String slashed = dot.matcher(string).replaceAll("/");
		Type type = Type.getObjectType(slashed);
		return DecodeResult.success(type);
	}

	@Override
	public JsonValue encode(Type value) {
		return new JsonString(value.getClassName());
	}
}
