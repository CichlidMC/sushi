package io.github.cichlidmc.sushi.test.def;

import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.CodecResult;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SimpleRegistryTests {
	@Test
	public void bootstrap() {
		SimpleRegistry<String> registry = SimpleRegistry.create(
				builder -> builder.register(new Id(Id.BUILT_IN_NAMESPACE, "test"), "h")
		);

		assertEquals("h", registry.get(new Id(Id.BUILT_IN_NAMESPACE, "test")));
	}

	@Test
	public void codec() {
		SimpleRegistry<String> registry = SimpleRegistry.create(builder -> {
			builder.setDefaultNamespace("gerald");
			builder.register(new Id("gerald", "test"), "h");
		});

		Codec<String> codec = registry.byIdCodec();
		CodecResult<String> result = codec.decode(new JsonString("test"));

		assertTrue(result.isSuccess(), () -> result.asError().message);

		assertEquals("h", result.getOrThrow());
	}

	@Test
	public void conflict() {
		Id id = new Id("test", "test");

		SimpleRegistry<String> registry = SimpleRegistry.create(builder -> {
			builder.register(id, "h");
			assertThrows(
					IllegalArgumentException.class,
					() -> builder.register(id, "h 2: the long awaited sequel")
			);
		});

		assertThrows(
				IllegalArgumentException.class,
				() -> registry.register(id, "h 3: this time it's personal")
		);
	}
}
