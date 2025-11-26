package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.condition.builtin.AllCondition;
import fish.cichlidmc.sushi.api.condition.builtin.AnyCondition;
import fish.cichlidmc.sushi.api.condition.builtin.NotCondition;
import fish.cichlidmc.sushi.api.condition.builtin.TransformerPresentCondition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.Hooks;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ConditionTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					
						void noop() {
						}
					}
					"""
			).withDefinition("head_transform", """
					{
						"target": "$target",
						"transforms": {
							"type": "inject",
							"method": "test",
							"point": "head",
							"hook": {
								"name": "inject",
								"class": "$hooks"
							}
						}
					}
					"""
			).withDefinition("tail_transform", """
					"target": "$target",
					"transforms": {
						"type": "inject",
						"method": "test",
						"point": "tail",
						"hook": {
							"name": "inject",
							"class": "$hooks"
						}
					},
					"""
			);

	@Test
	public void transformerPresent() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform(headTransformer(Optional.empty()))
		.transform(tailTransformer(Optional.of(
				new TransformerPresentCondition(new Id("tests", "0"))
		)))
		.expect("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		);
	}


	@Test
	public void transformerMissing() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
				).transform(headTransformer(Optional.empty()))
				.transform(tailTransformer(Optional.of(
						new TransformerPresentCondition(new Id("tests", "this_transformer_does_not_exist"))
				)))
				.expect("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		);
	}

	@Test
	public void complex() {
		factory.compile("""
				void test() {
					noop();
				}
				"""
		).transform(headTransformer(Optional.empty()))
				.transform(tailTransformer(Optional.of(
						new AllCondition(List.of(
								new AnyCondition(List.of(
										new TransformerPresentCondition(new Id("tests", "0")),
										new TransformerPresentCondition(new Id("tests", "this_does_not_exist"))
								)),
								new NotCondition(new TransformerPresentCondition(new Id("tests", "neither_does_this")))
						))
				)))
		.expect("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		);
	}

	private static Function<Id, ConfiguredTransformer> headTransformer(Optional<Condition> condition) {
		return transformer(HeadInjectionPoint.INSTANCE, condition);
	}

	private static Function<Id, ConfiguredTransformer> tailTransformer(Optional<Condition> condition) {
		return transformer(TailInjectionPoint.INSTANCE, condition);
	}

	private static Function<Id, ConfiguredTransformer> transformer(InjectionPoint point, Optional<Condition> condition) {
		Transformer transformer = new InjectTransformer(
				new SingleClassTarget(TestTarget.DESC),
				new MethodTarget("test"),
				Slice.NONE,
				new HookingTransformer.Hook(
						new HookingTransformer.Hook.Owner(Hooks.DESC),
						"inject"
				),
				false,
				point
		);

		return id -> new ConfiguredTransformer(id, transformer, condition, 0, 0);
	}
}
