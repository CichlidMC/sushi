package io.github.cichlidmc.sushi.test.def;

import io.github.cichlidmc.sushi.test.framework.TestFactory;
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
						"hook": "$hooks.inject"
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
						"hook": "$hooks.inject"
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
							"hook": "$hooks.inject"
						},
						{
							"type": "inject",
							"method": "test",
							"point": "tail",
							"hook": "$hooks.inject"
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
						"hook": "$hooks.thisHookDoesNotExist"
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
						"hook": "$hooks.inject"
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
						"hook": "$hooks.inject"
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
						"hook": "$hooks.inject"
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
}
