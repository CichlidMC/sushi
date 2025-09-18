package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class ModifyExpressionTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						int getInt() {
							return 0;
						}
					}
					"""
			);

	@Test
	public void simpleModifyInt() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "modify_expression",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": "getInt"
						},
						"modifier": {
							"name": "modifyInt",
							"class": "$hooks"
						}
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.modifyInt(getInt());
				}
				"""
		);
	}

	@Test
	public void modifyIntWithLocal() {
		factory.compile("""
				void test() {
					byte b = 0;
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "modify_expression",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": "getInt"
						},
						"modifier": {
							"name": "modifyIntWithLocal",
							"class": "$hooks",
							"parameters": [
								{
									"type": "local/slot",
									"slot": 1,
									"local_type": "byte"
								}
							]
						}
					}
				}
				"""
		).expect("""
				void test() {
					byte b = 0;
					Hooks.modifyIntWithLocal(getInt(), b);
				}
				"""
		);
	}

	@Test
	public void chainedModify() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": [
						{
							"type": "modify_expression",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": "getInt"
							},
							"modifier": {
								"name": "modifyInt",
								"class": "$hooks"
							}
						},
						{
							"type": "modify_expression",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": "getInt"
							},
							"modifier": {
								"name": "modifyInt",
								"class": "$hooks"
							}
						}
					]
				}
				"""
		).expect("""
				void test() {
					Hooks.modifyInt(Hooks.modifyInt(getInt()));
				}
				"""
		);
	}

	@Test
	public void missingModifier() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "modify_expression",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": "getInt"
						},
						"modifier": {
							"name": "thisModifierDoesNotExist",
							"class": "$hooks"
						}
					}
				}
				"""
		).fail();
	}

	@Test
	public void missingTarget() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "modify_expression",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": "thisMethodDoesNotExist"
						},
						"modifier": {
							"name": "modifyInt",
							"class": "$hooks"
						}
					}
				}
				"""
		).fail("""
				MethodTarget did not match the expected number of times
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.TestTarget
					- Phase: 0
					- Current Transformer: tests:0
					- Method: void test()
					- Actual Matches: 0
					- Expected Matches: 1
				"""
		);
	}

	@Test
	public void wrongType() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "modify_expression",
						"method": "test",
						"target": {
							"type": "invoke",
							"method": "getInt"
						},
						"modifier": {
							"name": "modifyObject",
							"class": "$hooks"
						}
					}
				}
				"""
		).fail();
	}
}
