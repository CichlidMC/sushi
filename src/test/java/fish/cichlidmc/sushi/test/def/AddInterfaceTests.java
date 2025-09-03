package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public class AddInterfaceTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withMetadata(true)
			.withDefinition("transform", """
					{
						"target": "$target$Inner",
						"transforms": {
							"type": "add_interface",
							"interface": "$hooks$ThingDoer"
						}
					}
					"""
			).withClassTemplate("""
					class TestTarget {
					%s
					}
					"""
			);

	@Test
	public void addInterface() {
		factory.compile("""
				class Inner {
				}
				"""
		).transform("$transform")
		.expect("""
				@TransformedBy({"tests:0"})
				@InterfaceAdded(
					by = {"tests:0"},
					value = ThingDoer.class
				)
				class Inner implements ThingDoer {
				}
				"""
		);
	}

	@Test
	public void addInterfaceTwice() {
		factory.compile("""
				class Inner {
				}
				"""
				).transform("$transform")
				.transform("$transform")
				.expect("""
				@TransformedBy({"tests:0", "tests:1"})
				@InterfaceAdded(
					by = {"tests:0", "tests:1"},
					value = ThingDoer.class
				)
				class Inner implements ThingDoer {
				}
				"""
				);
	}

	@Test
	public void addInterfaceAlreadyApplied() {
		factory.compile("""
				class Inner implements fish.cichlidmc.sushi.test.hooks.Hooks.ThingDoer {
				}
				"""
		).transform("$transform")
		.expect("""
				@TransformedBy({"tests:0"})
				@InterfaceAdded(
					by = {"tests:0"},
					value = ThingDoer.class
				)
				class Inner implements ThingDoer {
				}
				"""
		);
	}
}
