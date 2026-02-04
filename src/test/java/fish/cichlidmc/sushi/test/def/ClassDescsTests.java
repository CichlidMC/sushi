package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinyjson.value.JsonValue;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.junit.jupiter.api.Test;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ClassDescsTests {
	@Test
	public void fullNames() {
		assertEquals("java.lang.Object", ClassDescs.fullName(ConstantDescs.CD_Object));
		assertEquals("int", ClassDescs.fullName(ConstantDescs.CD_int));

		assertEquals("java.lang.Object[]", ClassDescs.fullName(ConstantDescs.CD_Object.arrayType()));
		assertEquals("java.lang.Object[][]", ClassDescs.fullName(ConstantDescs.CD_Object.arrayType(2)));

		assertEquals("int[]", ClassDescs.fullName(ConstantDescs.CD_int.arrayType()));
		assertEquals("int[][]", ClassDescs.fullName(ConstantDescs.CD_int.arrayType(2)));
	}

	@Test
	public void decodePrimitive() {
		JsonValue json = new JsonString("int");

		assertEquals(ConstantDescs.CD_int, ClassDescs.ANY_CODEC.decode(json).valueOrThrow());
		assertEquals(ConstantDescs.CD_int, ClassDescs.PRIMITIVE_CODEC.decode(json).valueOrThrow());
		assertTrue(ClassDescs.CLASS_CODEC.decode(json).isError());
		assertTrue(ClassDescs.ARRAY_CODEC.decode(json).isError());
	}

	@Test
	public void encodePrimitive() {
		ClassDesc desc = ConstantDescs.CD_int;

		assertEquals(new JsonString("int"), ClassDescs.ANY_CODEC.encode(desc).valueOrThrow());
		assertEquals(new JsonString("int"), ClassDescs.PRIMITIVE_CODEC.encode(desc).valueOrThrow());
		assertTrue(ClassDescs.CLASS_CODEC.encode(desc).isError());
		assertTrue(ClassDescs.ARRAY_CODEC.encode(desc).isError());
	}

	@Test
	public void decodeObject() {
		JsonValue json = new JsonString("java.lang.Object");

		assertEquals(ConstantDescs.CD_Object, ClassDescs.ANY_CODEC.decode(json).valueOrThrow());
		assertEquals(ConstantDescs.CD_Object, ClassDescs.CLASS_CODEC.decode(json).valueOrThrow());
		assertTrue(ClassDescs.PRIMITIVE_CODEC.decode(json).isError());
		assertTrue(ClassDescs.ARRAY_CODEC.decode(json).isError());
	}

	@Test
	public void encodeObject() {
		ClassDesc desc = ConstantDescs.CD_Object;

		assertEquals(new JsonString("java.lang.Object"), ClassDescs.ANY_CODEC.encode(desc).valueOrThrow());
		assertEquals(new JsonString("java.lang.Object"), ClassDescs.CLASS_CODEC.encode(desc).valueOrThrow());
		assertTrue(ClassDescs.PRIMITIVE_CODEC.encode(desc).isError());
		assertTrue(ClassDescs.ARRAY_CODEC.encode(desc).isError());
	}

	@Test
	public void decodeArray() {
		JsonValue json = new JsonString("int[][]");

		assertEquals(ConstantDescs.CD_int.arrayType(2), ClassDescs.ANY_CODEC.decode(json).valueOrThrow());
		assertEquals(ConstantDescs.CD_int.arrayType(2), ClassDescs.ARRAY_CODEC.decode(json).valueOrThrow());
		assertTrue(ClassDescs.CLASS_CODEC.decode(json).isError());
		assertTrue(ClassDescs.PRIMITIVE_CODEC.decode(json).isError());
	}

	@Test
	public void encodeArray() {
		ClassDesc desc = ConstantDescs.CD_int.arrayType(2);

		assertEquals(new JsonString("int[][]"), ClassDescs.ANY_CODEC.encode(desc).valueOrThrow());
		assertEquals(new JsonString("int[][]"), ClassDescs.ARRAY_CODEC.encode(desc).valueOrThrow());
		assertTrue(ClassDescs.CLASS_CODEC.encode(desc).isError());
		assertTrue(ClassDescs.PRIMITIVE_CODEC.encode(desc).isError());
	}
}
