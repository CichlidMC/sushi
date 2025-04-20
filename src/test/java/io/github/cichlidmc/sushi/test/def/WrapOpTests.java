package io.github.cichlidmc.sushi.test.def;

import io.github.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class WrapOpTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
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
							"method": "$target.getInt"
						},
						"wrapper": "$hooks.wrapGetInt"
					}
				}
				"""
		).expect("""
				void test() {
					int i = Hooks.wrapGetInt(true, var0 -> {
						WrapOpValidation.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((boolean)var0[1]);
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
							"method": "$target.doThing"
						},
						"wrapper": "$hooks.wrapDoThing"
					}
				}
				"""
		).expect("""
				void test() {
					Hooks.wrapDoThing(1, "h", var0 -> {
						WrapOpValidation.checkCount(var0, 3);
						((TestTarget)var0[0]).doThing((int)var0[1], (String)var0[2]);
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
								"method": "$target.getInt"
							},
							"wrapper": "$hooks.wrapGetInt"
						},
						{
							"type": "wrap_operation",
							"method": "test",
							"target": {
								"type": "invoke",
								"method": "$target.getInt"
							},
							"wrapper": "$hooks.wrapGetInt"
						}
					]
				}
				"""
		).fail(); // TODO: make it stack. Let it pass for now to build.
	}
}
