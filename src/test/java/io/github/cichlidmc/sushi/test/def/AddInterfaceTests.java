package io.github.cichlidmc.sushi.test.def;

import io.github.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public class AddInterfaceTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
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
				class Inner implements ThingDoer {
				}
				"""
		);
	}

	@Test
	public void addInterfaceAlreadyApplied() {
		factory.compile("""
				class Inner implements io.github.cichlidmc.sushi.test.hooks.Hooks.ThingDoer {
				}
				"""
		).transform("$transform").fail();
	}
}
