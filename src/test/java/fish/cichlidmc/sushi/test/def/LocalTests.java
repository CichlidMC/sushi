package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.expression.builtin.InvokeExpressionTarget;
import fish.cichlidmc.sushi.api.target.inject.builtin.ExpressionInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;

public final class LocalTests {
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
	public void selectName() {
		factory.compile("""
				double test() {
					int x = 1;
					double d = 4d;
					String s = "h";
					noop();
					return x * d + s.length();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(
										LocalContextParameter.forName("x", ConstantDescs.CD_int, false)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(x);
					noop();
					return x * d + s.length();
				}
				"""
		);
	}

	@Test
	public void selectThisBySlot() {
		factory.compile("""
				double test() {
					int x = 1;
					double d = 4d;
					String s = "h";
					noop();
					return x * d + s.length();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(
										LocalContextParameter.forSlot(0, TestTarget.DESC, false)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(this);
					noop();
					return x * d + s.length();
				}
				"""
		);
	}

	@Test
	public void selectThisByName() {
		factory.compile("""
				double test() {
					int x = 1;
					double d = 4d;
					String s = "h";
					noop();
					return x * d + s.length();
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(
										LocalContextParameter.forName("this", TestTarget.DESC, false)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(this);
					noop();
					return x * d + s.length();
				}
				"""
		);
	}

	@Test
	public void selectByNameComplexScope() {
		factory.compile("""
				void test() {
					int x = 0;
					if (x > 1) {
						String s = "h";
						if (s.endsWith("h")) {
							noop();
						}
					} else {
						Integer z = Integer.valueOf("12");
						int s = z.hashCode();
						String other = "a";
						noop();
					}
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.to(new ExpressionInjectionPoint(new InvokeExpressionTarget(
								new MethodTarget("valueOf", ConstantDescs.CD_Integer)
						))),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(
										LocalContextParameter.forName("s", ConstantDescs.CD_String, false)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				void test() {
					int x = 0;
					if (x > 1) {
						String s = "h";
						if (s.endsWith("h")) {
							Hooks.injectWithLocal(s);
							noop();
						}
					} else {
						Integer z = Integer.valueOf("12");
						int s = z.hashCode();
						String other = "a";
						noop();
					}
				}
				"""
		);
	}
}
