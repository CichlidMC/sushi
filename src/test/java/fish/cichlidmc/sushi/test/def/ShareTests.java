package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;

public final class ShareTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						void noop() {
						}
					}
					"""
			);

	// @Test // FIXME: this *works*, but the discard ends up above the second inject. Need a system to do something in a later phase
	public void shareHeadAndTail() {
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
								"name": "injectWithShare",
								"class": "$hooks",
								"parameters": [
									{
										"type": "share",
										"key": "tests:h",
										"value_type": "short"
									}
								]
							}
						},
						{
							"type": "inject",
							"method": "test",
							"point": "tail",
							"hook": {
								"name": "injectWithShare",
								"class": "$hooks",
								"parameters": [
									{
										"type": "share",
										"key": "tests:h",
										"value_type": "short"
									}
								]
							}
						}
					]
				}
				"""
		).expect("""
				void test() {
					ShortRefImpl var1 = new ShortRefImpl();
					Hooks.injectWithShare(var1);
					noop();
					Hooks.injectWithShare(var1);
					var1.discard();
				}
				"""
		);
	}
}
