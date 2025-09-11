package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class SliceTests {
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
	public void fromInvoke() {
		factory.compile("""
				void test() {
					noop();
					System.out.println("h");
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "sliced",
						"from": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": {
									"name": "println",
									"class": "java.io.PrintStream"
								}
							},
							"after": true
						},
						"wrapped": {
							"type": "inject",
							"method": "test",
							"point": "head",
							"hook": {
								"name": "inject",
								"class": "$hooks"
							}
						}
					}
				}
				"""
		).expect("""
				void test() {
					noop();
					System.out.println("h");
					Hooks.inject();
					noop();
				}
				"""
		);
	}

	@Test
	public void betweenInvokes() {
		factory.compile("""
				void test() {
					noop();
					System.out.print("a");
					noop();
					System.out.println("b");
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "sliced",
						"from": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": {
									"name": "print",
									"class": "java.io.PrintStream"
								}
							},
							"after": true
						},
						"to": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": {
									"name": "println",
									"class": "java.io.PrintStream"
								}
							}
						},
						"wrapped": {
							"type": "inject",
							"method": "test",
							"point": "head",
							"hook": {
								"name": "inject",
								"class": "$hooks"
							}
						}
					}
				}
				"""
		).expect("""
				void test() {
					noop();
					System.out.print("a");
					Hooks.inject();
					noop();
					System.out.println("b");
					noop();
				}
				"""
		);
	}
}
