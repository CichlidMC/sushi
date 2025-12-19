package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.expression.builtin.InvokeExpressionTarget;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;

public final class WrapOpTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withDefinition("operation", Operation.class.getName())
			.withClassTemplate("""
					class TestTarget {
					%s
					
						int getInt(boolean b) {
							return 0;
						}
					
						void doThing(int x, String s) {
						}
					}
					"""
			);

	@Test
	public void simpleWrapInvoke() {
		factory.compile("""
				void test() {
					int i = getInt(true);
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					});
				}
				"""
		);
	}

	@Test
	public void wrapVoidInvoke() {
		factory.compile("""
				void test() {
					doThing(1, "h");
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapDoThing"
						),
						new InvokeExpressionTarget(new MethodTarget("doThing"))
				)
		).expect("""
				void test() {
					Hooks.wrapDoThing(this, 1, "h", var0 -> {
						ExtractionValidation.checkCount(var0, 3);
						((TestTarget)var0[0]).doThing((Integer)var0[1], (String)var0[2]);
					});
				}
				"""
		);
	}

	@Test
	public void doubleWrapInvoke() {
		factory.compile("""
				void test() {
					int i = getInt(true);
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return Hooks.wrapGetInt((TestTarget)var0[0], (Boolean)var0[1], var0x -> {
							ExtractionValidation.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						});
					});
				}
				"""
		);
	}

	@Test
	public void wrapInvokeWithLocal() {
		factory.compile("""
				void test() {
					double d = 12;
					int i = getInt(d > 5);
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(LocalContextParameter.forSlot(1, ConstantDescs.CD_double, true))
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					double d = 12.0;
					boolean var10001 = d > 5.0;
					Operation var10002 = var0 -> {
						ExtractionValidation.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					};
					DoubleRefImpl var4 = new DoubleRefImpl(d);
					Hooks.wrapGetIntWithLocal(this, var10001, var10002, var4);
					d = var4.get();
					var4.discard();
				}
				"""
		);
	}

	// @Test // FIXME: need to implement local fixing
	public void doubleWrapWithLocals() {
		factory.compile("""
				void test() {
					double d = 12;
					int i = getInt(d > 5);
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).transform(
				new WrapOpTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(LocalContextParameter.forSlot(1, ConstantDescs.CD_double, true))
						),
						new InvokeExpressionTarget(new MethodTarget("getInt"))
				)
		).fail();
	}
}
