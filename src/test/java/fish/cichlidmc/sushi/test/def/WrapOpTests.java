package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.transform.infra.Operation;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class WrapOpTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withDefinition("operation", Operation.class.getName())
			.withClassTemplate("""
					class TestTarget {
					%s
					
						int getInt(boolean b) {
							return 0;
						}
					
						void doThing(int x, String s) {
						}
					}
					"""
			);

	@Test
	public void simpleWrapInvoke() {
		factory.compile("""
				void test() {
					int i = getInt(true);
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "wrap_operation",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": {
								"name": "getInt",
								"class": "$target"
							}
						},
						"wrapper": {
							"name": "wrapGetInt",
							"class": "$hooks"
						}
					}
				}
				"""
		).expect("""
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					});
				}
				"""
		);
	}

	@Test
	public void wrapVoidInvoke() {
		factory.compile("""
				void test() {
					doThing(1, "h");
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "wrap_operation",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": {
								"name": "doThing",
								"class": "$target"
							}
						},
						"wrapper": {
							"name": "wrapDoThing",
							"class": "$hooks"
						}
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.wrapDoThing(this, 1, "h", var0 -> {
						ExtractionValidation.checkCount(var0, 3);
						((TestTarget)var0[0]).doThing((Integer)var0[1], (String)var0[2]);
					});
				}
				"""
		);
	}

	@Test
	public void doubleWrapInvoke() {
		factory.compile("""
				void test() {
					int i = getInt(true);
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": [
						{
							"type": "wrap_operation",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": {
									"name": "getInt",
									"class": "$target"
								}
							},
							"wrapper": {
								"name": "wrapGetInt",
								"class": "$hooks"
							}
						},
						{
							"type": "wrap_operation",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": {
									"name": "getInt",
									"class": "$target"
								}
							},
							"wrapper": {
								"name": "wrapGetInt",
								"class": "$hooks"
							}
						}
					]
				}
				"""
		).expect("""
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return Hooks.wrapGetInt((TestTarget)var0[0], (Boolean)var0[1], var0x -> {
							ExtractionValidation.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						});
					});
				}
				"""
		);
	}

	@Test
	public void wrapInvokeWithLocal() {
		factory.compile("""
				void test() {
					double d = 12;
					int i = getInt(d > 5);
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "wrap_operation",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": {
								"name": "getInt",
								"class": "$target"
							}
						},
						"wrapper": {
							"name": "wrapGetIntWithLocal",
							"class": "$hooks",
							"parameters": [
								{
									"type": "local/slot",
									"slot": 1,
									"local_type": "double",
									"mutable": true
								}
							]
						}
					}
				}
				"""
		).expect("""
				void test() {
					double d = 12.0;
					boolean var10001 = d > 5.0;
					Operation var10002 = var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					};
					DoubleRefImpl var4 = new DoubleRefImpl(d);
					Hooks.wrapGetIntWithLocal(this, var10001, var10002, var4);
					d = var4.get();
					var4.discard();
				}
				"""
		);
	}

	// @Test // FIXME: need to implement local fixing
	public void doubleWrapWithLocals() {
		factory.compile("""
				void test() {
					double d = 12;
					int i = getInt(d > 5);
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "wrap_operation",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": {
								"name": "getInt",
								"class": "$target"
							}
						},
						"wrapper": {
							"name": "wrapGetInt",
							"class": "$hooks"
						}
					}
				}
				"""
		).transform("""
				{
					"target": "$target",
					"priority": 100,
					"transforms": {
						"type": "wrap_operation",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": {
								"name": "getInt",
								"class": "$target"
							}
						},
						"wrapper": {
							"name": "wrapGetIntWithLocal",
							"class": "$hooks",
							"parameters": [
								{
									"type": "local/slot",
									"slot": 1,
									"local_type": "double",
									"mutable": true
								}
							]
						}
					}
				}
				"""
		).fail();
	}
}
