package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class PhaseTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						void noop() {
						}
					}
					"""
			);

	@Test
	public void targetFirstTransform() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": "head",
						"hook": {
							"name": "inject",
							"class": "$hooks"
						}
					}
				}
				"""
		).transform("""
				{
					"target": "$target",
					"phase": 1,
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": "inject"
							}
						},
						"hook": {
							"name": "inject",
							"class": "$hooks"
						}
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
					Hooks.inject();
					noop();
				}
				"""
		);
	}
}
