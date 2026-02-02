package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.ExpressionPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

public final class PhaseTests {
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
	public void targetFirstTransform() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						HeadPointSelector.TARGET
				)
		).inPhase(new Id("tests", "late"), phase -> {
			phase.builder.runAfter(Phase.DEFAULT);
			phase.builder.withBarriers(Phase.Barriers.BEFORE_ONLY);

			phase.transform(new InjectTransformer(
					new SingleClassPredicate(TestTarget.DESC),
					new MethodTarget(new MethodSelector("test")),
					Slice.NONE,
					new HookingTransformer.Hook(
							new HookingTransformer.Hook.Owner(Hooks.DESC),
							"inject"
					),
					false,
					new PointTarget(new ExpressionPointSelector(
							new InvokeExpressionSelector(
									new MethodSelector("inject", Hooks.DESC)
							),
							Offset.BEFORE
					))
			));
		}).decompile("""
				void test() {
					Hooks.inject();
					Hooks.inject();
					noop();
				}
				"""
		).execute();
	}
}
