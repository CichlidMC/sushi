package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.MethodTarget;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.AnyClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.EverythingClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.inject.builtin.HeadPointSelector;
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

	private static Transformer transformer(ClassPredicate target) {
		return new InjectTransformer(
				target,
				new MethodTarget("test"),
				Slice.NONE,
				new HookingTransformer.Hook(
						new HookingTransformer.Hook.Owner(Hooks.DESC),
						"inject"
				),
				false,
				HeadPointSelector.INSTANCE
		);
	}

	@Test
	public void testSingleClass() {
		factory.compile("""
				void test() {
				}
				"""
		).transform(transformer(new SingleClassPredicate(TestTarget.DESC)))
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
		).transform(transformer(new SingleClassPredicate(SomeOtherClass.DESC)))
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
		).transform(transformer(new AnyClassPredicate(
				new SingleClassPredicate(TestTarget.DESC.nested("Inner1")),
				new SingleClassPredicate(TestTarget.DESC.nested("Inner2"))
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
		).transform(transformer(new AnyClassPredicate(
				new SingleClassPredicate(TestTarget.DESC.nested("Inner1")),
				new SingleClassPredicate(TestTarget.DESC.nested("Inner2"))
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
				transformer(EverythingClassPredicate.INSTANCE)
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
