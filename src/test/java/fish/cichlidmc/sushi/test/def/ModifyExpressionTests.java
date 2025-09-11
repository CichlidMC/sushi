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
