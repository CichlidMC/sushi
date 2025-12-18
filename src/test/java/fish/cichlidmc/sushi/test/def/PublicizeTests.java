package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.FieldTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeFieldTransformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import org.junit.jupiter.api.Test;

import java.lang.constant.ConstantDescs;

public final class PublicizeTests {
	private static final TestFactory factory = TestFactory.ROOT.fork().withMetadata(true);

	@Test
	public void publicizeClass() {
		factory.compile("""
				class TestTarget {
				}
				"""
		).transform(
				new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC))
		).expect("""
				@TransformedBy("tests:0")
				@PublicizedBy("tests:0")
				public class TestTarget {
				}
				"""
		);
	}

	@Test
	public void classAlreadyPublic() {
		factory.compile("""
				public class TestTarget {
				}
				"""
		).transform(
				new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC))
		).fail("""
				Class is already public
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Current Transformer: tests:0
				"""
		);
	}

	@Test
	public void publicizeClassTwice() {
		Transformer transformer = new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC));
		factory.compile("""
				class TestTarget {
				}
				"""
		).transform(transformer).transform(transformer)
		.expect("""
				@TransformedBy({"tests:0", "tests:1"})
				@PublicizedBy({"tests:0", "tests:1"})
				public class TestTarget {
				}
				"""
		);
	}

	@Test
	public void classPublicizedPreviousPhase() {
		factory.compile("""
				class TestTarget {
				}
				"""
		).transform(new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC)))
		.inPhase(new Id("tests", "early"), phase -> {
			phase.builder.runBefore(Phase.DEFAULT);
			phase.transform(new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC)));
		})
		.expect("""
				@TransformedBy({"tests:1", "tests:0"})
				@PublicizedBy({"tests:1", "tests:0"})
				public class TestTarget {
				}
				"""
		);
	}

	@Test
	public void publicizeInnerClass() {
		factory.compile("""
				public class TestTarget {
					private class Inner {
					}
				}
				"""
		).transform(
				new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC.nested("Inner")))
		).expect("""
				public class TestTarget {
					@TransformedBy("tests:0")
					@PublicizedBy("tests:0")
					public class Inner {
					}
				}
				"""
		);
	}

	@Test
	public void innerClassAlreadyPublic() {
		factory.compile("""
				public class TestTarget {
					public class Inner {
					}
				}
				"""
		).transform(
				new PublicizeClassTransformer(new SingleClassTarget(TestTarget.DESC.nested("Inner")))
		).fail("""
				Class is already public
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.infra.TestTarget$Inner
					- Current Transformer: tests:0
				"""
		);
	}

	@Test
	public void publicizeField() {
		factory.compile("""
				public class TestTarget {
					private int x;
				}
				"""
		).transform(
				new PublicizeFieldTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new FieldTarget("x")
				)
		).expect("""
				@TransformedBy("tests:0")
				public class TestTarget {
					@PublicizedBy("tests:0")
					public int x;
				}
				"""
		);
	}

	@Test
	public void fieldAlreadyPublic() {
		factory.compile("""
				public class TestTarget {
					public int x;
				}
				"""
		).transform(
				new PublicizeFieldTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new FieldTarget("x")
				)
		).fail("""
				Field is already public
				Details:
					- Class being Transformed: fish.cichlidmc.sushi.test.infra.TestTarget
					- Current Transformer: tests:0
					- Field: public x int
				"""
		);
	}

	@Test
	public void publicizeFieldWithType() {
		factory.compile("""
				public class TestTarget {
					private int x;
				}
				"""
		).transform(
				new PublicizeFieldTransformer(
						new SingleClassTarget(TestTarget.DESC),
						new FieldTarget("x", ConstantDescs.CD_int)
				)
		).expect("""
				@TransformedBy("tests:0")
				public class TestTarget {
					@PublicizedBy("tests:0")
					public int x;
				}
				"""
		);
	}
}
