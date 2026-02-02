package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.condition.builtin.AllCondition;
import fish.cichlidmc.sushi.api.condition.builtin.AnyCondition;
import fish.cichlidmc.sushi.api.condition.builtin.NotCondition;
import fish.cichlidmc.sushi.api.condition.builtin.TransformerPresentCondition;
import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointSelector;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.match.point.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.point.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
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
		))).decompile("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		).execute();
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
				))).decompile("""
				void test() {
					Hooks.inject();
					noop();
				}
				"""
		).execute();
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
				))).decompile("""
				void test() {
					Hooks.inject();
					noop();
					Hooks.inject();
				}
				"""
		).execute();
	}

	private static Function<Id, ConfiguredTransformer> headTransformer(Optional<Condition> condition) {
		return transformer(HeadPointSelector.INSTANCE, condition);
	}

	private static Function<Id, ConfiguredTransformer> tailTransformer(Optional<Condition> condition) {
		return transformer(TailPointSelector.INSTANCE, condition);
	}

	private static Function<Id, ConfiguredTransformer> transformer(PointSelector selector, Optional<Condition> condition) {
		Transformer transformer = new InjectTransformer(
				new SingleClassPredicate(TestTarget.DESC),
				new MethodTarget(new MethodSelector("test")),
				Slice.NONE,
				new HookingTransformer.Hook(
						new HookingTransformer.Hook.Owner(Hooks.DESC),
						"inject"
				),
				false,
				new PointTarget(selector)
		);

		return id -> new ConfiguredTransformer(id, transformer, condition);
	}
}
