package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.expression.builtin.InvokeExpressionTarget;
import fish.cichlidmc.sushi.api.target.inject.builtin.ExpressionInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						HeadInjectionPoint.INSTANCE
				)
		).inPhase(new Id("tests", "late"), phase -> {
			phase.builder.runAfter(Phase.DEFAULT);
			phase.builder.withBarriers(Phase.Barriers.BEFORE_ONLY);

			phase.transform(new InjectTransformer(
					new SingleClassTarget(TestTarget.DESC),
					new MethodTarget("test"),
					Slice.NONE,
					new HookingTransformer.Hook(
							new HookingTransformer.Hook.Owner(Hooks.DESC),
							"inject"
					),
					false,
					new ExpressionInjectionPoint(
							new InvokeExpressionTarget(
									new MethodTarget("inject", Hooks.DESC)
							),
							Offset.BEFORE
					)
			));
		}).expect("""
				void test() {
					Hooks.inject();
					Hooks.inject();
					noop();
				}
				"""
		);
	}
}
