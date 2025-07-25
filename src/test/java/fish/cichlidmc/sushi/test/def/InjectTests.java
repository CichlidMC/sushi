package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class InjectTests {
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
	public void simpleHeadInject() {
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
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		);
	}

	@Test
	public void simpleInjectTail() {
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
						"point": "tail",
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		);
	}

	@Test
	public void injectHeadAndTail() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": [
						{
							"type": "inject",
							"method": "test",
							"point": "head",
							"hook": {
								"name": "inject",
								"class": "$hooks",
								"parameters": [],
								"return": "$cancellation"
							}
						},
						{
							"type": "inject",
							"method": "test",
							"point": "tail",
							"hook": {
								"name": "inject",
								"class": "$hooks",
								"parameters": [],
								"return": "$cancellation"
							}
						}
					]
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		);
	}

	@Test
	public void missingHook() {
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
							"name": "thisMethodDoesNotExist",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).fail();
	}

	@Test
	public void implicitAllReturns() {
		factory.compile("""
				void test(boolean b) {
					if (b) {
						return;
					}
				
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": "return",
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
						Hooks.inject();
					}
				}
				"""
		);
	}

	@Test
	public void explicitAllReturns() {
		factory.compile("""
				void test(boolean b) {
					if (b) {
						return;
					}
				
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": {
							"type": "return",
							"index": -1
						},
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
						Hooks.inject();
					}
				}
				"""
		);
	}

	@Test
	public void firstReturn() {
		factory.compile("""
				void test(boolean b) {
					if (b) {
						return;
					}
				
					noop();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": {
							"type": "return",
							"index": 0
						},
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
					}
				}
				"""
		);
	}

	@Test
	public void beforeExpression() {
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
						"point": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": "$target.noop"
							}
						},
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		);
	}

	@Test
	public void afterExpression() {
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
						"point": {
							"type": "expression",
							"target": {
								"type": "invoke",
								"method": "$target.noop"
							},
							"after": true
						},
						"hook": {
							"name": "inject",
							"class": "$hooks",
							"parameters": [],
							"return": "$cancellation"
						}
					}
				}
				"""
		).expect("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		);
	}
}
