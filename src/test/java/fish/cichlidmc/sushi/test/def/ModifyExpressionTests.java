package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.expression.builtin.ConstructionExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.builtin.NewExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.ModifyExpressionTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.Map;

public final class ModifyExpressionTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						int getInt() {
							return 0;
						}
					}
					"""
			);

	@Test
	public void simpleModifyInt() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				void test() {
					Hooks.modifyInt(getInt());
				}
				"""
		).execute();
	}

	@Test
	public void modifyIntWithLocal() {
		factory.compile("""
				void test() {
					byte b = 0;
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyIntWithLocal",
								List.of(new LocalContextParameter.Immutable(1, ConstantDescs.CD_byte))
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				void test() {
					byte b = 0;
					Hooks.modifyIntWithLocal(getInt(), b);
				}
				"""
		).execute();
	}

	@Test
	public void chainedModify() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).decompile("""
				void test() {
					Hooks.modifyInt(Hooks.modifyInt(getInt()));
				}
				"""
		).execute();
	}

	@Test
	public void missingModifier() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"thisMethodDoesNotExist"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).fail();
	}

	@Test
	public void missingTarget() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("thisTargetDoesNotExist")))
				)
		).fail("""
				Target matched 0 times, expected 1
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: void test()
					- Target: ExpressionTarget[selector=InvokeExpressionSelector[selector=MethodSelector[name=thisTargetDoesNotExist]], expected=1]
				"""
		);
	}

	@Test
	public void wrongType() {
		factory.compile("""
				void test() {
					getInt();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyObject"
						),
						new ExpressionTarget(new InvokeExpressionSelector(new MethodSelector("getInt")))
				)
		).fail();
	}

	@Test
	public void modifyConstruct() {
		factory.compile("""
				void test() {
					Object o = new Object();
					String s = o.toString();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyObject"
						),
						new ExpressionTarget(new ConstructionExpressionSelector((ConstantDescs.CD_Object)))
				)
		).decompile("""
				void test() {
					Object o = Hooks.modifyObject(new Object());
					String s = o.toString();
				}
				"""
		).execute();
	}

	@Test
	public void modifyNewArray() {
		factory.compile("""
				void test() {
					int[] ints = {1, 2, 3};
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyIntArray"
						),
						new ExpressionTarget(new NewExpressionSelector(ClassDescs.of(int[].class)))
				)
		).decompile("""
				void test() {
					int[] var10000 = Hooks.modifyIntArray(new int[3]);
					var10000[0] = 1;
					var10000[1] = 2;
					var10000[2] = 3;
				}
				"""
		).execute();
	}

	@Test
	public void modifyWithCoercion() {
		factory.compile("""
				String test() {
					record InaccessibleType(String s) {}
					InaccessibleType gerald = new InaccessibleType("abc");
					return gerald.toString();
				}
				"""
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget(new MethodSelector("test")),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyWithCoercion",
								HookingTransformer.Hook.Coercions.of(Map.of(
										TestTarget.DESC.nested("1InaccessibleType"),
										ClassDescs.of(Record.class)
								)),
								List.of()
						),
						new ExpressionTarget(new ConstructionExpressionSelector(TestTarget.DESC.nested("1InaccessibleType")))
				)
		).decompile("""
				String test() {
					record InaccessibleType(String s) {
					}
				
					InaccessibleType gerald = (InaccessibleType)Hooks.modifyWithCoercion(new InaccessibleType("abc"));
					return gerald.toString();
				}
				"""
		).invoke(
				"test", List.of(), "InaccessibleType[s=abc]"
		).execute();
	}
}
