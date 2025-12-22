package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.param.builtin.ShareContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;

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

	@Test
	public void shareHeadAndTail() {
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
								"injectWithShare",
								List.of(
										new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short)
								)
						),
						false,
						HeadInjectionPoint.INSTANCE
				)
		).transform(
				new InjectTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithShare",
								List.of(
										new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short)
								)
						),
						false,
						TailInjectionPoint.INSTANCE
				)
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
