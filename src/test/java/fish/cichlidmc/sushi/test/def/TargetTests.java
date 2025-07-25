package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public class TargetTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withDefinition("transform", """
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
					"""
			).withClassTemplate("""
					class TestTarget {
					%s
					}
					"""
			);

	@Test
	public void testSingleClass() {
		factory.compile("""
				void test() {
				}
				"""
		).transform("""
				{
					"target": "$target",
					$transform
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
				}
				"""
		);
	}

	@Test
	public void testSingleClassMissing() {
		factory.compile("""
				void test() {
				}
				"""
		).transform("""
				{
					"target": "ThisClassDoesNotExist",
					$transform
				}
				"""
		).expect("""
				void test() {
				}
				"""
		);
	}

	@Test
	public void testArray() {
		factory.compile("""
				class Inner1 {
					void test() {
					}
				}
				
				class Inner2 {
					void test() {
					}
				}
				"""
		).transform("""
				{
					"target": [ "$target$Inner1", "$target$Inner2" ],
					$transform
				}
				"""
		).expect("""
				class Inner1 {
					void test() {
						Hooks.inject();
					}
				}
				
				class Inner2 {
					void test() {
						Hooks.inject();
					}
				}
				"""
		);
	}

	@Test
	public void testArrayMissingOne() {
		factory.compile("""
				class Inner1 {
					void test() {
					}
				}
				"""
		).transform("""
				{
					"target": [ "$target$Inner1", "$target$Inner2" ],
					$transform
				}
				"""
		).expect("""
				class Inner1 {
					void test() {
						Hooks.inject();
					}
				}
				"""
		);
	}

	@Test
	public void testEverything() {
		factory.compile("""
				void test() {
				}
				
				class Inner {
					void test() {
					}
				}
				"""
		).transform("""
				{
					"target": {
						"type": "everything"
					},
					$transform
				}
				"""
		).expect("""
				void test() {
					Hooks.inject();
				}
				
				class Inner {
					void test() {
						Hooks.inject();
					}
				}
				"""
		);
	}
}
