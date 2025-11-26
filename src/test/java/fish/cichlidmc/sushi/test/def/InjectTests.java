package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.model.code.Offset;
import fish.cichlidmc.sushi.api.param.builtin.local.SlottedLocalContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.expression.builtin.InvokeExpressionTarget;
import fish.cichlidmc.sushi.api.target.inject.builtin.ExpressionInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.ReturnInjectionPoint;
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
		).expect("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						TailInjectionPoint.INSTANCE
				)
		).expect("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		);
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
						TailInjectionPoint.INSTANCE
				)
		).expect("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"thisMethodDoesNotExist"
						),
						false,
						HeadInjectionPoint.INSTANCE
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						ReturnInjectionPoint.ALL
				)
		).expect("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
						Hooks.inject();
					}
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						new ReturnInjectionPoint(0)
				)
		).expect("""
				void test(boolean b) {
					if (b) {
						Hooks.inject();
					} else {
						noop();
					}
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"inject"
						),
						false,
						new ExpressionInjectionPoint(
								new InvokeExpressionTarget(new MethodTarget("noop")),
								Offset.AFTER
						)
				)
		).expect("""
				void test() {
					noop();
					Hooks.inject();
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectAndCancel"
						),
						true,
						HeadInjectionPoint.INSTANCE
				)
		).expect("""
				void test() {
					if (Hooks.injectAndCancel() == null) {
						noop();
					}
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectAndCancel"
						),
						true,
						HeadInjectionPoint.INSTANCE
				)
		).expect("""
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
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithLocal",
								List.of(
										new SlottedLocalContextParameter(1, ConstantDescs.CD_int, false)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
				int test() {
					int x = 1;
					Hooks.injectWithLocal(x);
					noop();
					return x;
				}
				"""
		);
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
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithMutableLocal",
								List.of(
										new SlottedLocalContextParameter(1, ConstantDescs.CD_int, true)
								)
						),
						false,
						new ExpressionInjectionPoint(new InvokeExpressionTarget(new MethodTarget("noop")))
				)
		).expect("""
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
		);
	}
}
