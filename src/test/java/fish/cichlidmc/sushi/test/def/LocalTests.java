package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.ExpressionPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(new LocalContextParameter.Immutable("x", ConstantDescs.CD_int))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(x);
					noop();
					return x * d + s.length();
				}
				"""
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(new LocalContextParameter.Immutable(0, TestTarget.DESC))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(this);
					noop();
					return x * d + s.length();
				}
				"""
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(new LocalContextParameter.Immutable("this", TestTarget.DESC))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				double test() {
					int x = 1;
					double d = 4.0;
					String s = "h";
					Hooks.injectWithLocal(this);
					noop();
					return x * d + s.length();
				}
				"""
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.to(new ExpressionPointSelector(new InvokeExpressionSelector(
								new MethodSelector("valueOf", ConstantDescs.CD_Integer)
						))),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(new LocalContextParameter.Immutable("s", ConstantDescs.CD_String))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
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
		).execute();
	}

	@Test
	public void checkHeadScopes() {
		// we do some shenanigans with local scopes at the method's head, make sure
		// a local right at the top is correctly not found when injecting at head
		factory.compile("""
				double test(boolean bl) {
					int x = 1;
					return Math.sqrt(x);
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocals",
								List.of(
										new LocalContextParameter.Immutable("x", ConstantDescs.CD_int),
										new LocalContextParameter.Immutable("bl", ConstantDescs.CD_boolean)
								)
						),
						false,
						HeadPointSelector.TARGET
				)
		).fail("""
				No local variable found with name x
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: double test(boolean)
				"""
		);
	}
}
