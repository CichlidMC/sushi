package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.transform.wrap_op.Operation;
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
								"class": "$target",
								"parameters": ["boolean"],
								"return": "int"
							}
						},
						"wrapper": {
							"name": "wrapGetInt",
							"class": "$hooks",
							"parameters": [
								"$target",
								"boolean",
								"$operation"
							],
							"return": "int"
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
								"class": "$target",
								"parameters": [
									"int",
									"java.lang.String"
								],
								"return": "void"
							}
						},
						"wrapper": {
							"name": "wrapDoThing",
							"class": "$hooks",
							"parameters": [
								"$target",
								"int",
								"java.lang.String",
								"$operation"
							],
							"return": "void"
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
									"class": "$target",
									"parameters": ["boolean"],
									"return": "int"
								}
							},
							"wrapper": {
								"name": "wrapGetInt",
								"class": "$hooks",
								"parameters": [
									"$target",
									"boolean",
									"$operation"
								],
								"return": "int"
							}
						},
						{
							"type": "wrap_operation",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": {
									"name": "getInt",
									"class": "$target",
									"parameters": ["boolean"],
									"return": "int"
								}
							},
							"wrapper": {
								"name": "wrapGetInt",
								"class": "$hooks",
								"parameters": [
									"$target",
									"boolean",
									"$operation"
								],
								"return": "int"
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
}
