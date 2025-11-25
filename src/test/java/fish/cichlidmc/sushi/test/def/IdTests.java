package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.tinycodecs.api.CodecResult;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class IdTests {
	@Test
	public void validNamespace() {
		assertTrue(Id.isValidNamespace("test_namespace_1"));
	}

	@Test
	public void invalidNamespaceDash() {
		assertFalse(Id.isValidNamespace("test-namespace"));
	}

	@Test
	public void invalidNamespaceOther() {
		assertFalse(Id.isValidNamespace("a!./#$%"));
	}

	@Test
	public void validPath() {
		assertTrue(Id.isValidPath("test_path/1"));
	}

	@Test
	public void invalidPathDash() {
		assertFalse(Id.isValidPath("test-path"));
	}

	@Test
	public void invalidPathOther() {
		assertFalse(Id.isValidPath("a!.@#$%"));
	}

	@Test
	public void defaultCodecNoNamespace() {
		CodecResult<Id> result = Id.CODEC.decode(new JsonString("test"));
		if (result instanceof CodecResult.Success(Id id)) {
			fail(id.toString());
		}
	}

	@Test
	public void defaultCodec() {
		Id expected = new Id(Sushi.NAMESPACE, "test");
		CodecResult<Id> result = Id.CODEC.decode(new JsonString("sushi:test"));

		if (result instanceof CodecResult.Error(String message)) {
			fail(message);
		}

		assertEquals(expected, result.getOrThrow());
	}

	@Test
	public void customCodec() {
		CodecResult<Id> result = Id.fallbackNamespaceCodec("gerald").decode(new JsonString("test"));

		if (result instanceof CodecResult.Error(String message)) {
			fail(message);
		}

		assertEquals(new Id("gerald", "test"), result.getOrThrow());
	}

	@Test
	public void parseSuccess() {
		assertNotNull(Id.parseOrNull(Sushi.NAMESPACE, "test"));
	}

	@Test
	public void parseFail() {
		assertNull(Id.parseOrNull(Sushi.NAMESPACE, "aaaaaa!!!!!!"));
	}

	@Test
	public void createFail() {
		assertThrows(Id.InvalidException.class, () -> new Id("aaa!!!", "bad!!!"));
	}
}
