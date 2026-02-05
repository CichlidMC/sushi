package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.EverythingClassPredicate;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.ThrowingConsumer;
import fish.cichlidmc.sushi.test.framework.TestUtils;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.classfile.instruction.InvokeInstruction;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MiscTests {
	@BeforeAll
	public static void registerTestTransformer() {
		Transformer.REGISTRY.register(new Id("misc_tests", "test_transformer"), TestTransformer.codec);
	}

	@Test
	public void checkModuleVersion() {
		Sushi.class.getModule().getDescriptor().version().orElseThrow();
	}

	@Test
	public void selectionContains() {
		transform(context -> {
			TransformableCode code = context.target().methods().values().stream()
					.filter(method -> method.key().name().equals("doStuff"))
					.findFirst()
					.flatMap(TransformableMethod::code)
					.orElseThrow();

			Point head = Point.before(code.instructions().getFirst());
			Point tail = Point.after(code.instructions().getLast());

			InstructionHolder<?> println = code.instructions().stream()
					.filter(instruction -> instruction.get() instanceof InvokeInstruction)
					.findFirst()
					.orElseThrow();

			Selection whole = code.select().from(head).to(tail);
			Selection onlyPrintln = code.select().only(println);

			assertTrue(whole.contains(head, true, false));
			assertTrue(whole.contains(tail, false, true));
			assertFalse(whole.contains(head, false, true));
			assertFalse(whole.contains(tail, true, false));

			assertTrue(whole.contains(head.instruction()));
			assertTrue(whole.contains(tail.instruction()));
			assertTrue(whole.contains(println));

			assertFalse(onlyPrintln.contains(head, true, true));
			assertFalse(onlyPrintln.contains(head.instruction()));
			assertFalse(onlyPrintln.contains(tail, true, true));
			assertFalse(onlyPrintln.contains(tail.instruction()));
			assertTrue(onlyPrintln.contains(println));
			assertTrue(onlyPrintln.contains(Point.before(println), true, true));
			assertFalse(onlyPrintln.contains(Point.before(println), false, true));
			assertTrue(onlyPrintln.contains(Point.after(println), true, true));
			assertFalse(onlyPrintln.contains(Point.after(println), true, false));
		});
	}

	private static void transform(ThrowingConsumer<TransformContext, TransformException> consumer) {
		TransformerManager.Builder builder = TransformerManager.builder();
		builder.defaultPhase().register(new ConfiguredTransformer(
				new Id("misc_tests", "main"), new TestTransformer(consumer)
		));
		TransformerManager manager = builder.build();

		byte[] bytes = TestUtils.getBytes(TestClass.class);
		Optional<TransformResult> result = manager.transform(bytes, null);
		assertTrue(result.isPresent());
	}

	private static class TestClass {
		@SuppressWarnings("unused")
		private void doStuff(int x) {
			double d = x * 1.5;
			System.out.println("d=" + d);
		}
	}

	private record TestTransformer(ThrowingConsumer<TransformContext, TransformException> consumer) implements SimpleTransformer {
		private static final MapCodec<TestTransformer> codec = MapCodec.lazy(() -> {
			throw new RuntimeException();
		});

		@Override
		public void apply(TransformContext context) throws TransformException {
			this.consumer.accept(context);
		}

		@Override
		public ClassPredicate classPredicate() {
			return EverythingClassPredicate.INSTANCE;
		}

		@Override
		public MapCodec<? extends Transformer> codec() {
			return codec;
		}
	}
}
