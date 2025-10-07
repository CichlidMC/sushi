package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class ConditionTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						void noop() {
						}
					}
					"""
			).withDefinition("head_transform", """
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
			).withDefinition("tail_transform", """
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": "tail",
						"hook": {
							"name": "inject",
							"class": "$hooks"
						}
					},
					"""
			);

	@Test
	public void transformerPresent() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform("$head_transform").transform("""
				{
					$tail_transform
					"condition": {
						"type": "transformer_present",
						"id": "tests:0"
					}
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
	public void transformerMissing() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform("$head_transform").transform("""
				{
					$tail_transform
					"condition": {
						"type": "transformer_present",
						"id": "tests:this_transformer_does_not_exist"
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
	public void complex() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform("$head_transform").transform("""
				{
					$tail_transform
					"condition": {
						"type": "all",
						"conditions": [
							{
								"type": "any",
								"conditions": [
									{
										"type": "transformer_present",
										"id": "tests:0"
									},
									{
										"type": "transformer_present",
										"id": "tests:this_does_not_exist"
									}
								]
							},
							{
								"type": "not",
								"condition": {
									"type": "transformer_present",
									"id": "tests:neither_does_this"
								}
							}
						]
					}
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
}
