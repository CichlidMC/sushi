package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.expression.builtin.ConstructionExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.param.builtin.ShareContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.Map;

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
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						OperationInfra.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					});
				}
				"""
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapDoThing"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("doThing")))
				)
		).decompile("""
				void test() {
					Hooks.wrapDoThing(this, 1, "h", var0 -> {
						OperationInfra.checkCount(var0, 3);
						((TestTarget)var0[0]).doThing((Integer)var0[1], (String)var0[2]);
						return null;
					});
				}
				"""
		).invoke(
				"test", List.of(), null
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
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
				void test() {
					int i = Hooks.wrapGetInt(this, true, var0 -> {
						OperationInfra.checkCount(var0, 2);
						return Hooks.wrapGetInt((TestTarget)var0[0], (Boolean)var0[1], var0x -> {
							OperationInfra.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						});
					});
				}
				"""
		).execute();
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
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(new LocalContextParameter.Mutable(1, ConstantDescs.CD_double))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				void test() {
					double d = 12.0;
					boolean var10001 = d > 5.0;
					Operation var10002 = var0 -> {
						OperationInfra.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getInt((Boolean)var0[1]);
					};
					DoubleRefImpl var4 = new DoubleRefImpl(d);
					Hooks.wrapGetIntWithLocal(this, var10001, var10002, var4);
					d = var4.get();
					var4.discard();
				}
				"""
		).execute();
	}

	@Test
	public void doubleWrapWithLocals() {
		factory.compile("""
				void test() {
					double d = 12;
					int i = getInt(d > 5);
				}
				"""
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
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(new LocalContextParameter.Mutable(1, ConstantDescs.CD_double))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				void test() {
					double d = 12.0;
					boolean var10001 = d > 5.0;
					DoubleRefImpl var5 = new DoubleRefImpl(d);
					Hooks.wrapGetInt(this, var10001, var1x -> {
						OperationInfra.checkCount(var1x, 2);
						TestTarget var10000 = (TestTarget)var1x[0];
						boolean var10001x = (Boolean)var1x[1];
						Operation var10002 = var0x -> {
							OperationInfra.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						};
						DoubleRefImpl var4 = new DoubleRefImpl(((DoubleRefImpl)var5).get());
						int var5x = Hooks.wrapGetIntWithLocal(var10000, var10001x, var10002, var4);
						DoubleRefImpl.set(var4.get(), (DoubleRefImpl)var5);
						var4.discard();
						return var5x;
					});
					d = var5.get();
					var5.discard();
				}
				"""
		).execute();
	}

	@Test
	public void doubleWrapWithShare() {
		factory.compile("""
				String test(double d) {
					short s = 12;
					int i = getInt(d > 5);
					return String.valueOf(i);
				}
				"""
		).transform(
				// transformer 1: just wrap
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
		).transform(
				// transformer 2: wrap with share
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapWithShare",
								List.of(new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).transform(
				// transformer 3: inject with share
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithShare",
								List.of(new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short))
						),
						false,
						TailPointSelector.TARGET
				)
		).decompile("""
				String test(double d) {
					ShortRefImpl var5 = new ShortRefImpl();
					short s = 12;
					int i = Hooks.wrapGetInt(this, d > 5.0, var1x -> {
						OperationInfra.checkCount(var1x, 2);
						return Hooks.wrapWithShare((TestTarget)var1x[0], (Boolean)var1x[1], var0x -> {
							OperationInfra.checkCount(var0x, 2);
							return ((TestTarget)var0x[0]).getInt((Boolean)var0x[1]);
						}, (ShortRefImpl)var5);
					});
					String var10000 = String.valueOf(i);
					Hooks.injectWithShare(var5);
					var5.discard();
					return var10000;
				}
				"""
		).execute();
	}

	@Test
	public void attemptRareAndDangerous5xWrapCombo() {
		factory.compile("""
				String test(double d) {
					short s = 12;
					int i = getInt(d > 5);
					return String.valueOf(i);
				}
				"""
		).transform(
				// transformer 1: just wrap
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
		).transform(
				// transformer 2: wrap with share
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapWithShare",
								List.of(new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).transform(
				// transformer 3: inject with share
				new InjectTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"injectWithShare",
								List.of(new ShareContextParameter(new Id("tests", "h"), ConstantDescs.CD_short))
						),
						false,
						TailPointSelector.TARGET
				)
		).transform(
				// transformer 4: wrap with mutable local
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(new LocalContextParameter.Mutable("d", ConstantDescs.CD_double))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).transform(
				// transformer 5: wrap with different local
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapGetIntWithLocal",
								List.of(new LocalContextParameter.Immutable("s", ConstantDescs.CD_short))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
				// I pinky promise this is correct (at least, I'm pretty sure)
		).decompile("""
				String test(double d) {
					ShortRefImpl var5 = new ShortRefImpl();
					short s = 12;
					boolean var10001 = d > 5.0;
					DoubleRefImpl var8 = new DoubleRefImpl(d);
					int var10000 = Hooks.wrapGetInt(this, var10001, var3x -> {
						OperationInfra.checkCount(var3x, 2);
						TestTarget var10000x = (TestTarget)var3x[0];
						boolean var10001x = (Boolean)var3x[1];
						DoubleRefImpl var7 = new DoubleRefImpl(((DoubleRefImpl)var8).get());
						int var8x = Hooks.wrapWithShare(var10000x, var10001x, var2x -> {
							OperationInfra.checkCount(var2x, 2);
							TestTarget var10000xx = (TestTarget)var2x[0];
							boolean var10001xx = (Boolean)var2x[1];
							Operation var10002 = var1xxx -> {
								OperationInfra.checkCount(var1xxx, 2);
								return Hooks.wrapGetIntWithLocal((TestTarget)var1xxx[0], (Boolean)var1xxx[1], var0xxx -> {
									OperationInfra.checkCount(var0xxx, 2);
									return ((TestTarget)var0xxx[0]).getInt((Boolean)var0xxx[1]);
								}, s);
							};
							DoubleRefImpl var6 = new DoubleRefImpl(((DoubleRefImpl)var7).get());
							int var7x = Hooks.wrapGetIntWithLocal(var10000xx, var10001xx, var10002, var6);
							DoubleRefImpl.set(var6.get(), (DoubleRefImpl)var7);
							var6.discard();
							return var7x;
						}, (ShortRefImpl)var5);
						DoubleRefImpl.set(var7.get(), (DoubleRefImpl)var8);
						var7.discard();
						return var8x;
					});
					d = var8.get();
					var8.discard();
					int i = var10000;
					String var10 = String.valueOf(i);
					Hooks.injectWithShare(var5);
					var5.discard();
					return var10;
				}
				"""
		).execute();
	}

	@Test
	public void wrapConstruct() {
		factory.compile("""
				void test() {
					Object s = "abc";
					StringBuilder builder = new StringBuilder(s.toString());
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapConstruct"
						),
						new ExpressionTarget(new ConstructionExpressionSelector(ClassDescs.of(StringBuilder.class)))
				)
		).decompile("""
				void test() {
					Object s = "abc";
					StringBuilder builder = Hooks.wrapConstruct(var1x -> {
						OperationInfra.checkCount(var1x, 0);
						return new StringBuilder(s.toString());
					});
				}
				"""
		).execute();
	}

	@Test
	public void wrapWithCoercion() {
		factory.compile("""
				String test() {
					record InaccessibleType(String s) {}
					InaccessibleType gerald = new InaccessibleType("123");
					return gerald.toString();
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapWithCoercion",
								HookingTransformer.Hook.Coercions.of(Map.of(
										TestTarget.DESC.nested("1InaccessibleType"), ConstantDescs.CD_Object
								)),
								List.of()
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("toString")))
				)
		).decompile("""
				String test() {
					record InaccessibleType(String s) {
					}
				
					InaccessibleType gerald = new InaccessibleType("123");
					return Hooks.wrapWithCoercion(gerald, var0 -> {
						OperationInfra.checkCount(var0, 1);
						return ((InaccessibleType)var0[0]).toString();
					});
				}
				"""
		).invoke(
				"test", List.of(), "InaccessibleType[s=123]!"
		).execute();
	}

	@Test
	public void invalidCoercion() {
		factory.compile("""
				String test() {
					record InaccessibleType(String s) {}
					InaccessibleType gerald = new InaccessibleType("123");
					return gerald.toString();
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapWithCoercion",
								HookingTransformer.Hook.Coercions.of(Map.of(
										TestTarget.DESC.nested("1InaccessibleType"), ConstantDescs.CD_String
								)),
								List.of()
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("toString")))
				)
		).fail("One or more requirements are unmet");
	}

	@Test
	public void coerceArray() {
		factory.fork().withClassTemplate("""
				class TestTarget {
				%s
				}
				"""
		).compile("""
				record InnerType(int x) {}
				
				int getLength(InnerType[] array) {
					return array.length;
				}
				
				int test() {
					return getLength(new InnerType[1]);
				}
				"""
		).transform(
				new WrapOpTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"wrapArrayWithCoercion",
								HookingTransformer.Hook.Coercions.of(Map.of(
										TestTarget.DESC.nested("InnerType").arrayType(),
										ClassDescs.of(Record.class).arrayType()
								)),
								List.of()
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getLength")))
				)
		).decompile("""
				int getLength(TestTarget.InnerType[] array) {
					return array.length;
				}
				
				int test() {
					return Hooks.wrapArrayWithCoercion(this, new TestTarget.InnerType[1], var0 -> {
						OperationInfra.checkCount(var0, 2);
						return ((TestTarget)var0[0]).getLength((TestTarget.InnerType[])var0[1]);
					});
				}
				
				record InnerType(int x) {
				}
				"""
		).invoke(
				"test", List.of(), 2
		).execute();
	}
}
