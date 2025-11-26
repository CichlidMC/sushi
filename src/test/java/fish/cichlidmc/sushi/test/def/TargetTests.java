package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.EverythingClassTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.builtin.UnionClassTarget;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.SomeOtherClass;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

public class TargetTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					}
					"""
			);

	private static Transformer transformer(ClassTarget target) {
		return new InjectTransformer(
				target,
				new MethodTarget("test"),
				Slice.NONE,
				new HookingTransformer.Hook(
						new HookingTransformer.Hook.Owner(Hooks.DESC),
						"inject"
				),
				false,
				HeadInjectionPoint.INSTANCE
		);
	}

	@Test
	public void testSingleClass() {
		factory.compile("""
				void test() {
				}
				"""
		).transform(transformer(new SingleClassTarget(TestTarget.DESC)))
		.expect("""
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
		).transform(transformer(new SingleClassTarget(SomeOtherClass.DESC)))
		.expect("""
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
		).transform(transformer(new UnionClassTarget(
				new SingleClassTarget(TestTarget.DESC.nested("Inner1")),
				new SingleClassTarget(TestTarget.DESC.nested("Inner2"))
		))).expect("""
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
		).transform(transformer(new UnionClassTarget(
				new SingleClassTarget(TestTarget.DESC.nested("Inner1")),
				new SingleClassTarget(TestTarget.DESC.nested("Inner2"))
		))).expect("""
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
		).transform(
				transformer(EverythingClassTarget.INSTANCE)
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
