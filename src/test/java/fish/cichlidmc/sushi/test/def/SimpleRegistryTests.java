package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SimpleRegistryTests {
	@Test
	public void bootstrap() {
		Id id = Sushi.id("test");
		SimpleRegistry<String> registry = SimpleRegistry.create(Sushi.NAMESPACE);
		registry.register(id, "h");
		assertEquals("h", registry.get(id));
	}

	@Test
	public void codec() {
		SimpleRegistry<String> registry = SimpleRegistry.create("gerald");
		registry.register(new Id("gerald", "test"), "h");

		Codec<String> codec = registry.byIdCodec();
		CodecResult<String> result = codec.decode(new JsonString("test"));

		assertTrue(result.isSuccess(), () -> result.asError().message);

		assertEquals("h", result.getOrThrow());
	}

	@Test
	public void conflict() {
		Id id = new Id("test", "test");

		SimpleRegistry<String> registry = SimpleRegistry.create(Sushi.NAMESPACE);
		registry.register(id, "h");

		assertThrows(
				IllegalArgumentException.class,
				() -> registry.register(id, "h 2: the long awaited sequel")
		);
	}
}
