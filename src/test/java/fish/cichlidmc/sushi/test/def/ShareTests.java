package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.param.builtin.ShareContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithShare",
								List.of(
										new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short)
								)
						),
						false,
						HeadPointSelector.TARGET
				)
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithShare",
								List.of(
										new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short)
								)
						),
						false,
						TailPointSelector.TARGET
				)
		).decompile("""
				void test() {
					ShortRefImpl var1 = new ShortRefImpl();
					Hooks.injectWithShare(var1);
					noop();
					Hooks.injectWithShare(var1);
					var1.discard();
				}
				"""
		).execute();
	}
}
