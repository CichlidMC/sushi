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
							"method": "$target.getInt"
						},
						"modifier": {
							"name": "modifyInt",
							"class": "$hooks",
							"parameters": ["int"],
							"return": "int"
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
								"method": "$target.getInt"
							},
							"modifier": {
								"name": "modifyInt",
								"class": "$hooks",
								"parameters": ["int"],
								"return": "int"
							}
						},
						{
							"type": "modify_expression",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": "$target.getInt"
							},
							"modifier": {
								"name": "modifyInt",
								"class": "$hooks",
								"parameters": ["int"],
								"return": "int"
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
							"method": "$target.getInt"
						},
						"modifier": "$hooks.thisModifierDoesNotExist"
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
							"method": "$target.thisMethodDoesNotExist"
						},
						"modifier": "$hooks.modifyInt"
					}
				}
				"""
		).fail();
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
							"method": "$target.getInt"
						},
						"modifier": "$hooks.modifyObject"
					}
				}
				"""
		).fail();
	}
}
