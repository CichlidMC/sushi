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
							"return": "void"
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
							"return": "void"
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
								"return": "void"
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
								"return": "void"
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
							"return": "void"
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
							"return": "void"
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
							"return": "void"
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
							"return": "void"
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
							"return": "void"
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
							"return": "void"
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
	public void cancelVoid() {
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
							"name": "injectAndCancel",
							"class": "$hooks"
						},
						"cancellable": true
					}
				}
				"""
		).expect("""
				void test() {
					if (Hooks.injectAndCancel() == null) {
						noop();
					}
				}
				"""
		);
	}

	@Test
	public void cancelInt() {
		factory.compile("""
				int test() {
					noop();
					return 0;
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
							"name": "injectAndCancel",
							"class": "$hooks"
						},
						"cancellable": true
					}
				}
				"""
		).expect("""
				int test() {
					Cancellation var10000 = Hooks.injectAndCancel();
					if (var10000 != null) {
						return (Integer)var10000.value;
					} else {
						noop();
						return 0;
					}
				}
				"""
		);
	}

	@Test
	public void injectWithLocal() {
		factory.compile("""
				int test() {
					int x = 1;
					noop();
					return x;
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
							"name": "injectWithLocal",
							"class": "$hooks",
							"parameters": [
								{
									"type": "local/slot",
									"slot": 1,
									"local_type": "int"
								}
							]
						}
					}
				}
				"""
		).expect("""
				int test() {
					int x = 1;
					Hooks.injectWithLocal(x);
					noop();
					return x;
				}
				"""
		);
	}

	@Test
	public void injectWithMutableLocal() {
		factory.compile("""
				int test() {
					int x = 1;
					noop();
					return x;
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
							"name": "injectWithMutableLocal",
							"class": "$hooks",
							"parameters": [
								{
									"type": "local/slot",
									"slot": 1,
									"local_type": "int",
									"mutable": true
								}
							]
						}
					}
				}
				"""
		).expect("""
				int test() {
					int x = 1;
					IntRefImpl var2;
					Hooks.injectWithMutableLocal(var2 = new IntRefImpl(x));
					x = var2.get();
					var2.discard();
					noop();
					return x;
				}
				"""
		);
	}
}
