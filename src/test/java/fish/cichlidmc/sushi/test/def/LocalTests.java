package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.SlicedExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.ExpressionPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
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
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(new LocalContextParameter.Immutable("s", ConstantDescs.CD_String))
						),
						false,
						new PointTarget(new ExpressionPointSelector(SlicedExpressionSelector.to(
								new ExpressionPointSelector(new InvokeExpressionSelector(
										new MethodSelector("valueOf", ConstantDescs.CD_Integer)
								)),
								new InvokeExpressionSelector(new MethodSelector("noop"))
						)))
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

	@Test
	public void ensureStackingDoesNotOverwrite() {
		factory.compile("""
				int test() {
					int i = 0;
					noop();
					return i;
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"incrementLocalInt",
								List.of(new LocalContextParameter.Mutable("i", ConstantDescs.CD_int))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("noop")))
				)
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"incrementLocalInt",
								List.of(new LocalContextParameter.Mutable("i", ConstantDescs.CD_int))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("noop")))
				)
		).decompile("""
				int test() {
					int i = 0;
					IntRefImpl var3 = new IntRefImpl(i);
					Operation var10001 = var1x -> {
						OperationInfra.checkCount(var1x, 1);
						TestTarget var10000 = (TestTarget)var1x[0];
						Operation var10001x = var0x -> {
							OperationInfra.checkCount(var0x, 1);
							((TestTarget)var0x[0]).noop();
							return null;
						};
						IntRefImpl var2 = new IntRefImpl(((IntRefImpl)var3).get());
						Hooks.incrementLocalInt(var10000, var10001x, var2);
						IntRefImpl.set(var2.get(), (IntRefImpl)var3);
						var2.discard();
						return null;
					};
					IntRefImpl var4 = new IntRefImpl(i);
					Hooks.incrementLocalInt(this, var10001, var4);
					i = var4.get();
					var4.discard();
					i = var3.get();
					var3.discard();
					return i;
				}
				"""
		).invoke(
				"test", List.of(), 2
		).execute();
	}
}
