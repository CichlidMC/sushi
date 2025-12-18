package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.builtin.AddInterfaceTransformer;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import fish.cichlidmc.sushi.test.infra.ThingDoer;
import org.junit.jupiter.api.Test;

public class AddInterfaceTests {
	private static final TestFactory factory = TestFactory.ROOT.fork().withMetadata(true);

	@Test
	public void addInterface() {
		factory.compile("""
				class TestTarget {
				}
				"""
		).transform(
				new AddInterfaceTransformer(
						new SingleClassTarget(TestTarget.DESC),
						ThingDoer.DESC
				)
		).expect("""
				@TransformedBy("tests:0")
				@InterfaceAdded(by = "tests:0", value = ThingDoer.class)
				class TestTarget implements ThingDoer {
				}
				"""
		);
	}

	@Test
	public void addInterfaceTwice() {
		Transformer transformer = new AddInterfaceTransformer(
				new SingleClassTarget(TestTarget.DESC),
				ThingDoer.DESC
		);

		factory.compile("""
				class TestTarget {
				}
				"""
		).transform(transformer).transform(transformer)
		.expect("""
				@TransformedBy({"tests:0", "tests:1"})
				@InterfaceAdded(by = {"tests:0", "tests:1"}, value = ThingDoer.class)
				class TestTarget implements ThingDoer {
				}
				"""
		);
	}

	@Test
	public void addInterfaceAlreadyApplied() {
		factory.compile("""
				class TestTarget implements fish.cichlidmc.sushi.test.infra.ThingDoer {
				}
				"""
		).transform(
				new AddInterfaceTransformer(
						new SingleClassTarget(TestTarget.DESC),
						ThingDoer.DESC
				)
		).fail("""
				Interface being added is already on the target class
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Current Transformer: tests:0
				"""
		);
	}
}
