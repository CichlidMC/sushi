package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.ExpressionPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

public final class SliceTests {
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
	public void fromInvoke() {
		factory.compile("""
				void test() {
					noop();
					System.out.println("h");
					noop();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new Slice(
								new ExpressionPointSelector(
										new InvokeExpressionSelector(new MethodSelector("println")),
										Offset.AFTER
								),
								TailPointSelector.INSTANCE
						),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						HeadPointSelector.TARGET
				)
		).decompile("""
				void test() {
					noop();
					System.out.println("h");
					Hooks.inject();
					noop();
				}
				"""
		).execute();
	}

	@Test
	public void betweenInvokes() {
		factory.compile("""
				void test() {
					noop();
					System.out.print("a");
					noop();
					System.out.println("b");
					noop();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new Slice(
								new ExpressionPointSelector(
										new InvokeExpressionSelector(new MethodSelector("print")),
										Offset.AFTER
								),
								new ExpressionPointSelector(
										new InvokeExpressionSelector(new MethodSelector("println")),
										Offset.BEFORE
								)
						),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						HeadPointSelector.TARGET
				)
		).decompile("""
				void test() {
					noop();
					System.out.print("a");
					Hooks.inject();
					noop();
					System.out.println("b");
					noop();
				}
				"""
		).execute();
	}
}
