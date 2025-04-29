package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	public void defaultCodec() {
		CodecResult<Id> result = Id.CODEC.decode(new JsonString("test"));
		assertTrue(result.isSuccess(), () -> result.asError().message);
		assertEquals(new Id(Id.BUILT_IN_NAMESPACE, "test"), result.getOrThrow());
	}

	@Test
	public void customCodec() {
		CodecResult<Id> result = Id.fallbackNamespaceCodec("gerald").decode(new JsonString("test"));
		assertTrue(result.isSuccess(), () -> result.asError().message);
		assertEquals(new Id("gerald", "test"), result.getOrThrow());
	}

	@Test
	public void parseSuccess() {
		assertNotNull(Id.parseOrNull(Id.BUILT_IN_NAMESPACE, "test"));
	}

	@Test
	public void parseFail() {
		assertNull(Id.parseOrNull(Id.BUILT_IN_NAMESPACE, "aaaaaa!!!!!!"));
	}

	@Test
	public void createFail() {
		assertThrows(Id.InvalidException.class, () -> new Id("aaa!!!", "bad!!!"));
	}
}
