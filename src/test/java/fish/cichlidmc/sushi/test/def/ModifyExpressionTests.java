package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.MethodTarget;
import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.builtin.InvokeExpressionSelector;
import fish.cichlidmc.sushi.api.param.builtin.LocalContextParameter;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.ModifyExpressionTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;
import java.util.List;

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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					Hooks.modifyInt(getInt());
				}
				"""
		);
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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyIntWithLocal",
								List.of(
										LocalContextParameter.forSlot(1, ConstantDescs.CD_byte, false)
								)
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					byte b = 0;
					Hooks.modifyIntWithLocal(getInt(), b);
				}
				"""
		);
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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
				)
		).transform(
				new ModifyExpressionTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
				)
		).expect("""
				void test() {
					Hooks.modifyInt(Hooks.modifyInt(getInt()));
				}
				"""
		);
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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"thisMethodDoesNotExist"
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyInt"
						),
						new InvokeExpressionSelector(new MethodTarget("thisTargetDoesNotExist"))
				)
		).fail("""
				MethodTarget did not match the expected number of times
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Current Transformer: tests:0
					- Method: void test()
					- Actual Matches: 0
					- Expected Matches: 1
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
						new MethodTarget("test"),
						Slice.NONE,
						new HookingTransformer.Hook(
								new HookingTransformer.Hook.Owner(Hooks.DESC),
								"modifyObject"
						),
						new InvokeExpressionSelector(new MethodTarget("getInt"))
				)
		).fail();
	}
}
