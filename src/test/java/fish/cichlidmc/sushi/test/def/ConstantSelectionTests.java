package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.expression.builtin.ConstantExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.ModifyExpressionTransformer;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;

public final class ConstantSelectionTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					}
					"""
			);

	@Test
	public void selectString() {
		factory.compile("""
				String test() {
					return "abc";
				}
				"""
		).transform(
				transformer("modifyString", "abc")
		).decompile("""
				String test() {
					return Hooks.modifyString("abc");
				}
				"""
		).invoke(
				"test", List.of(), "abcabc"
		).execute();
	}

	@Test
	public void selectSmallInt() {
		// this should use ILOAD_0
		factory.compile("""
				int test() {
					return 0;
				}
				"""
		).transform(
				transformer("modifyInt", 0)
		).decompile("""
				int test() {
					return Hooks.modifyInt(0);
				}
				"""
		).invoke(
				"test", List.of(), 2
		).execute();
	}

	@Test
	public void selectLargeInt() {
		// this should use SIPUSH
		factory.compile("""
				int test() {
					return 1000;
				}
				"""
		).transform(
				transformer("modifyInt", 1000)
		).decompile("""
				int test() {
					return Hooks.modifyInt(1000);
				}
				"""
		).invoke(
				"test", List.of(), 1002
		).execute();
	}

	@Test
	public void selectFloat() {
		factory.compile("""
				float test() {
					return 0.5f;
				}
				"""
		).transform(
				transformer("modifyFloat", 0.5f)
		).decompile("""
				float test() {
					return Hooks.modifyFloat(0.5F);
				}
				"""
		).invoke(
				"test", List.of(), 0.25f
		).execute();
	}

	@Test
	public void trySelectFloatWithLong() {
		factory.compile("""
				float test() {
					return 1f;
				}
				"""
		).transform(
				transformer("modifyFloat", 1L)
		).fail("""
				Target matched 0 times, expected 1
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: float test()
					- Target: ExpressionTarget[selector=ConstantExpressionSelector[constant=1], expected=1]
				"""
		);
	}

	@Test
	public void selectClass() {
		// don't invoke this one, will fail since classes are loader-isolated
		factory.compile("""
				Class<?> test() {
					return Object.class;
				}
				"""
		).transform(
				transformer("modifyClass", ConstantDescs.CD_Object)
		).decompile("""
				Class<?> test() {
					return Hooks.modifyClass(Object.class);
				}
				"""
		).execute();
	}

	@Test
	public void trySelectPrimitiveClass() {
		// despite how it looks, this is secretly converted into a reference to Integer.TYPE.
		// I made sure PrimitiveClassDescImpl would work, but it seems that isn't even used in this case.
		factory.compile("""
				Class<?> test() {
					return int.class;
				}
				"""
		).transform(
				transformer("modifyClass", ConstantDescs.CD_int)
		).fail("""
				Target matched 0 times, expected 1
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: java.lang.Class test()
					- Target: ExpressionTarget[selector=ConstantExpressionSelector[constant=PrimitiveClassDesc[int]], expected=1]
				"""
		);
	}

	@Test
	public void trySelectEnum() {
		// this is also just a field get, not an EnumDesc.
		factory.compile("""
				Object test() {
					enum TestEnum { INSTANCE }
					return TestEnum.INSTANCE;
				}
				"""
		).transform(
				transformer("modifyObject", TestTarget.DESC.nested("1TestEnum"))
		).fail("""
				Target matched 0 times, expected 1
				Details:
					- Class being transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Transformers: default[-> tests:0 <-]
					- Method: java.lang.Object test()
					- Target: ExpressionTarget[selector=ConstantExpressionSelector[constant=ClassOrInterfaceDesc[TestTarget$1TestEnum]], expected=1]
				"""
		);
	}

	private static Transformer transformer(String hook, ConstantDesc constant) {
		return new ModifyExpressionTransformer(
				new SingleClassPredicate(TestTarget.DESC),
				new MethodTarget(new MethodSelector("test")),
				new HookingTransformer.Hook(new HookingTransformer.Hook.Owner(Hooks.DESC), hook),
				new ExpressionTarget(new ConstantExpressionSelector(constant))
		);
	}
}
