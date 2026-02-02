package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapMethodTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.framework.TestResult.Success.Invocation.Parameter;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;

public final class WrapMethodTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
						static final Object obj = new Object();
						double d;
					
					%s
					
						int getInt(boolean bl) {
							return 0;
						}
					
						static void noop() {
						}
					}
					"""
			);

	@Test
	public void wrapTrivial() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapTrivialMethod"
						)
				)
		).decompile("""
				void test() {
					Hooks.wrapTrivialMethod(this, var0 -> {
						OperationInfra.checkCount(var0, 1);
						TestTarget var1 = (TestTarget)var0[0];
						noop();
						return null;
					});
				}
				"""
		).invoke(
				"test", List.of(), null
		).execute();
	}

	@Test
	public void wrapWithParametersAndReturn() {
		factory.compile("""
				int test(boolean bl) {
					return getInt(bl);
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntMethod"
						)
				)
		).decompile("""
				int test(boolean var1) {
					return Hooks.wrapGetIntMethod(this, var1, var0 -> {
						OperationInfra.checkCount(var0, 2);
						TestTarget var1x = (TestTarget)var0[0];
						boolean var2 = (Boolean)var0[1];
						return var1x.getInt(var2);
					});
				}
				"""
		).execute();
	}

	@Test
	public void wrapStatic() {
		factory.compile("""
				static int test(boolean bl) {
					return bl ? 1 : 0;
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntStaticMethod"
						)
				)
		).decompile("""
				static int test(boolean var0) {
					return Hooks.wrapGetIntStaticMethod(var0, var0x -> {
						OperationInfra.checkCount(var0x, 1);
						boolean var1 = (Boolean)var0x[0];
						return var1 ? 1 : 0;
					});
				}
				"""
		).execute();
	}

	@Test
	public void multiWrap() {
		factory.compile("""
				int test(boolean bl, Object o) {
					int i = getInt(bl && o != null);
					if (i > 2) {
						noop();
					}
					this.d += i;
					return i;
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"multiWrap"
						)
				)
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"multiWrap"
						)
				)
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"multiWrap"
						)
				)
		).decompile("""
				int test(boolean var1, Object var2) {
					return Hooks.multiWrap(this, var1, var2, var0 -> {
						OperationInfra.checkCount(var0, 3);
						TestTarget var1x = (TestTarget)var0[0];
						boolean var2x = (Boolean)var0[1];
						Object var3 = var0[2];
						return Hooks.multiWrap(var1x, var2x, var3, var0x -> {
							OperationInfra.checkCount(var0x, 3);
							TestTarget var1xx = (TestTarget)var0x[0];
							boolean var2xx = (Boolean)var0x[1];
							Object var3x = var0x[2];
							return Hooks.multiWrap(var1xx, var2xx, var3x, var0xx -> {
								OperationInfra.checkCount(var0xx, 3);
								TestTarget var1xxx = (TestTarget)var0xx[0];
								boolean var2xxx = (Boolean)var0xx[1];
								Object var3xx = var0xx[2];
								int var4 = var1xxx.getInt(var2xxx && var3xx != null);
								if (var4 > 2) {
									noop();
								}
				
								var1xxx.d += var4;
								return var4;
							});
						});
					});
				}
				"""
		).execute();
	}

	@Test
	public void wrapConstructor() {
		factory.compile("""
				TestTarget(double d) {
					this.d = d;
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector(
								"<init>",
								MethodSelector.Desc.of(List.of(ConstantDescs.CD_double))
						)),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"thisDoesntMatter"
						)
				)
		).fail("""
				Constructors cannot be wrapped
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: void <init>(double)
				"""
		);
	}

	@Test
	public void wrapStaticInit() {
		factory.compile("").transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("<clinit>")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"thisDoesntMatter"
						)
				)
		).fail("""
				Static init cannot be wrapped
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: static void <clinit>()
				"""
		);
	}

	@Test
	public void wrapOtherTransforms() {
		factory.compile("""
				int test(boolean bl) {
					return getInt(bl);
				}
				"""
		).transform(
				new WrapMethodTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntMethod"
						)
				)
		).transform(
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(LocalContextParameter.forName(
										"bl",
										ConstantDescs.CD_boolean,
										false
								))
						),
						false,
						HeadPointSelector.TARGET
				)
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				int test(boolean var1) {
					return Hooks.wrapGetIntMethod(this, var1, var0 -> {
						OperationInfra.checkCount(var0, 2);
						TestTarget var1x = (TestTarget)var0[0];
						boolean var2 = (Boolean)var0[1];
						Hooks.injectWithLocal(var2);
						return Hooks.wrapGetInt(var1x, var2, var0x -> {
							OperationInfra.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						});
					});
				}
				"""
		).invoke(
				"test", List.of(new Parameter(boolean.class, false)), 0
		).execute();
	}
}
