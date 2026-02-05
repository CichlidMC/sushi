package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.builtin.ConstructionExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.NewExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.ExpressionPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.ReturnPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.StringJoiner;

public final class InjectTests {
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
	public void simpleHeadInject() {
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
		).decompile("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		).invoke(
				"test", List.of(), null
		).execute();
	}

	@Test
	public void simpleInjectTail() {
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
						TailPointSelector.TARGET
				)
		).decompile("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		).execute();
	}

	@Test
	public void injectHeadAndTail() {
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
						TailPointSelector.TARGET
				)
		).decompile("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		).execute();
	}

	@Test
	public void missingHook() {
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
								"thisMethodDoesNotExist"
						),
						false,
						HeadPointSelector.TARGET
				)
		).fail();
	}

	@Test
	public void implicitAllReturns() {
		factory.compile("""
				void test(boolean b) {
					if (b) {
						return;
					}
				
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
						ReturnPointSelector.ALL_TARGET
				)
		).decompile("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
						Hooks.inject();
					}
				}
				"""
		).execute();
	}

	@Test
	public void firstReturn() {
		factory.compile("""
				void test(boolean b) {
					if (b) {
						return;
					}
				
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
						new PointTarget(new ReturnPointSelector(0))
				)
		).decompile("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
					}
				}
				"""
		).execute();
	}

	@Test
	public void beforeExpression() {
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
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		).execute();
	}

	@Test
	public void afterExpression() {
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
						new PointTarget(new ExpressionPointSelector(
								new InvokeExpressionSelector(new MethodSelector("noop")),
								Offset.AFTER
						))
				)
		).decompile("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		).execute();
	}

	@Test
	public void cancelVoid() {
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
								"injectAndCancel"
						),
						true,
						HeadPointSelector.TARGET
				)
		).decompile("""
				void test() {
					if (Hooks.injectAndCancel() == null) {
						noop();
					}
				}
				"""
		).execute();
	}

	@Test
	public void cancelInt() {
		factory.compile("""
				int test() {
					noop();
					return 0;
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectAndCancel"
						),
						true,
						HeadPointSelector.TARGET
				)
		).decompile("""
				int test() {
					Cancellation var10000 = Hooks.injectAndCancel();
					if (var10000 != null) {
						return (Integer)var10000.value;
					} else {
						noop();
						return 0;
					}
				}
				"""
		).execute();
	}

	@Test
	public void injectWithLocal() {
		factory.compile("""
				int test() {
					int x = 1;
					noop();
					return x;
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
								List.of(new LocalContextParameter.Immutable(1, ConstantDescs.CD_int))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				int test() {
					int x = 1;
					Hooks.injectWithLocal(x);
					noop();
					return x;
				}
				"""
		).execute();
	}

	@Test
	public void injectWithMutableLocal() {
		factory.compile("""
				int test() {
					int x = 1;
					noop();
					return x;
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithMutableLocal",
								List.of(new LocalContextParameter.Mutable(1, ConstantDescs.CD_int))
						),
						false,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("noop"))))
				)
		).decompile("""
				int test() {
					int x = 1;
					IntRefImpl var2 = new IntRefImpl(x);
					Hooks.injectWithMutableLocal(var2);
					x = var2.get();
					var2.discard();
					noop();
					return x;
				}
				"""
		).execute();
	}

	@Test
	public void newObject() {
		factory.compile("""
				void test() {
					java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
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
						new PointTarget(new ExpressionPointSelector(new NewExpressionSelector(
								ClassDescs.of(StringJoiner.class)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					new StringJoiner(", ");
				}
				"""
		).execute();
	}

	@Test
	public void newPrimitiveArray() {
		factory.compile("""
				void test() {
					int[] ints = new int[3];
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
						new PointTarget(new ExpressionPointSelector(new NewExpressionSelector(
								ClassDescs.of(int[].class)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					int[] ints = new int[3];
				}
				"""
		).execute();
	}

	@Test
	public void newReferenceArray() {
		factory.compile("""
				void test() {
					java.util.StringJoiner[] joiners = new java.util.StringJoiner[2];
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
						new PointTarget(new ExpressionPointSelector(new NewExpressionSelector(
								ClassDescs.of(StringJoiner[].class)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					StringJoiner[] joiners = new StringJoiner[2];
				}
				"""
		).execute();
	}

	@Test
	public void newPrimitiveMultidimensionalArray() {
		factory.compile("""
				void test() {
					int[][] ints2d = new int[3][3];
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
						new PointTarget(new ExpressionPointSelector(new NewExpressionSelector(
								ClassDescs.of(int[][].class)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					int[][] ints2d = new int[3][3];
				}
				"""
		).execute();
	}

	@Test
	public void newReferenceMultidimensionalArray() {
		factory.compile("""
				void test() {
					java.util.StringJoiner[][][] joiners3d = new java.util.StringJoiner[2][3][4];
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
						new PointTarget(new ExpressionPointSelector(new NewExpressionSelector(
								ClassDescs.of(StringJoiner[][].class)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					StringJoiner[][][] joiners3d = new StringJoiner[2][3][4];
				}
				"""
		).execute();
	}

	@Test
	public void beforeConstruct() {
		factory.compile("""
				void test() {
					Object o = new Object();
					String s = o.toString();
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
						new PointTarget(new ExpressionPointSelector(new ConstructionExpressionSelector(
								(ConstantDescs.CD_Object)
						)))
				)
		).decompile("""
				void test() {
					Hooks.inject();
					Object o = new Object();
					String s = o.toString();
				}
				"""
		).execute();
	}

	@Test
	public void afterConstruct() {
		factory.compile("""
				void test() {
					Object o = new Object();
					String s = o.toString();
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
						new PointTarget(new ExpressionPointSelector(
								new ConstructionExpressionSelector((ConstantDescs.CD_Object)),
								Offset.AFTER
						))
				)
		).decompile("""
				void test() {
					Object var10000 = new Object();
					Hooks.inject();
					Object o = var10000;
					String s = o.toString();
				}
				"""
		).execute();
	}

	@Test
	public void headTailInjectsWithTry() {
		factory.compile("""
				void test(boolean bl) {
					try {
						noop();
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
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
						TailPointSelector.TARGET
				)
		).decompile("""
				void test(boolean bl) {
					Hooks.inject();
				
					try {
						noop();
					} catch (RuntimeException var3) {
						var3.printStackTrace();
					}
				
					Hooks.inject();
				}
				"""
		).execute();
	}

	@Test
	public void coercedLocal() {
		factory.compile("""
				String test() {
					String s = String.valueOf(123);
					System.out.println(s);
					return s + 456;
				}
				"""
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithCoercedLocal",
								List.of(new LocalContextParameter.Immutable(1, ConstantDescs.CD_String, ConstantDescs.CD_Object))
						),
						true,
						new PointTarget(new ExpressionPointSelector(new InvokeExpressionSelector(new MethodSelector("println"))))
				)
		).decompile("""
				String test() {
					String s = String.valueOf(123);
					PrintStream var10000 = System.out;
					Cancellation var10002 = Hooks.injectWithCoercedLocal(s);
					if (var10002 != null) {
						return (String)var10002.value;
					} else {
						var10000.println(s);
						return s + "456";
					}
				}
				"""
		).invoke(
				"test", List.of(), "!!!"
		).execute();
	}
}
