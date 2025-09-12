package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.test.framework.TestFactory;
import org.junit.jupiter.api.Test;

public final class PublicizeTests {
	private static final TestFactory factory = TestFactory.ROOT.fork().withMetadata(true);

	@Test
	public void publicizeClass() {
		factory.compile("""
				class TestTarget {
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "publicize/class"
					}
				}
				"""
		).expect("""
				@TransformedBy({"tests:0"})
				@PublicizedBy({"tests:0"})
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
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "publicize/class"
					}
				}
				"""
		).fail();
	}

	// @Test FIXME: this currently fails because only the class is modified, but the inner class attribute also needs to be modified.
	public void publicizeInnerClass() {
		factory.compile("""
				public class TestTarget {
					private class Inner {
					}
				}
				"""
		).transform("""
				{
					"target": "$target$Inner",
					"transforms": {
						"type": "publicize/class"
					}
				}
				"""
		).expect("""
				public class TestTarget {
					@TransformedBy({"tests:0"})
					@PublicizedBy({"tests:0"})
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
		).transform("""
				{
					"target": "$target$Inner",
					"transforms": {
						"type": "publicize/class"
					}
				}
				"""
		).fail();
	}

	@Test
	public void publicizeField() {
		factory.compile("""
				public class TestTarget {
					private int x;
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "publicize/field",
						"field": "x"
					}
				}
				"""
		).expect("""
				@TransformedBy({"tests:0"})
				public class TestTarget {
					@PublicizedBy({"tests:0"})
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
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "publicize/field",
						"field": "x"
					}
				}
				"""
		).fail();
	}

	@Test
	public void publicizeFieldWithType() {
		factory.compile("""
				public class TestTarget {
					private int x;
				}
				"""
		).transform("""
				{
					"target": "$target",
					"transforms": {
						"type": "publicize/field",
						"field": {
							"name": "x",
							"type": "int"
						}
					}
				}
				"""
		).expect("""
				@TransformedBy({"tests:0"})
				public class TestTarget {
					@PublicizedBy({"tests:0"})
					public int x;
				}
				"""
		);
	}
}
