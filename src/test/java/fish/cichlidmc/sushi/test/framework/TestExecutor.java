package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.LazyClassModel;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.test.framework.compiler.FileManager;
import fish.cichlidmc.sushi.test.framework.compiler.SourceObject;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.ClassFile;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TestExecutor {
	public static void execute(String source, List<String> transformers, Optional<String> expectedOutput) {
		boolean executed = false;

		try {
			doExecute(source, transformers, expectedOutput);
			executed = true;
		} catch (RuntimeException e) {
			if (expectedOutput.isPresent()) {
				throw e;
			}
		}

		if (executed && expectedOutput.isEmpty()) {
			Assertions.fail("Test did not fail, but should've");
		}
	}

	private static void doExecute(String source, List<String> transformers, Optional<String> expectedOutput) {
		Map<String, byte[]> output = compile(source);

		TransformerManager manager = prepareTransformers(transformers);
		Map<String, byte[]> transformed = transform(manager, output);

		Map<String, String> decompiled = TestUtils.DECOMPILER.decompile(transformed);

		if (expectedOutput.isEmpty())
			return;

		boolean onlyOne = decompiled.size() == 1;
		String mainOutput = decompiled.values().stream()
				.filter(s -> onlyOne || s.contains("class TestTarget {"))
				.findFirst()
				.map(TestExecutor::cleanupDecompile)
				.orElse(null);

		try {
			Assertions.assertEquals(expectedOutput.get(), mainOutput);
		} catch (AssertionFailedError e) {
			transformed.forEach((name, bytes) -> dumpBytes(Paths.get(name.substring(name.lastIndexOf('.') + 1) + ".class"), bytes));
			throw e;
		}
	}

	private static Map<String, byte[]> compile(String source) {
		StandardJavaFileManager standardManager = TestUtils.COMPILER.getStandardFileManager(null, null, null);
		FileManager manager = new FileManager(standardManager);

		List<JavaFileObject> input = List.of(new SourceObject(TestFactory.TEST_TARGET_CLASS_NAME, source));
		List<String> options = List.of("-g"); // include debugging info, like LVT names
		JavaCompiler.CompilationTask task = TestUtils.COMPILER.getTask(null, manager, null, options, null, input);
		if (!task.call() || manager.outputs.isEmpty()) {
			throw new RuntimeException("Compilation failed");
		}

		return manager.outputs.stream().collect(Collectors.toMap(
				output -> output.className,
				output -> output.bytes.toByteArray()
		));
	}

	private static TransformerManager prepareTransformers(List<String> transformers) {
		TransformerManager.Builder builder = TransformerManager.builder();
		builder.addMetadata(false);
		builder.withValidation(Validation.runtime(MethodHandles.lookup()));

		for (int i = 0; i < transformers.size(); i++) {
			String transformer = transformers.get(i);
			JsonValue json = TinyJson.parseOrThrow(transformer);
			Id id = new Id("tests", String.valueOf(i));
			builder.parseAndRegister(id, json).ifPresent(error -> {
				throw new RuntimeException("Failed to register transformer: " + error);
			});
		}

		return builder.build();
	}

	private static Map<String, byte[]> transform(TransformerManager manager, Map<String, byte[]> input) {
		Map<String, byte[]> transformed = new HashMap<>();

		input.forEach((name, bytes) -> {
			ClassDesc desc = ClassDesc.of(name);
			ClassFile context = ClassFile.of();
			LazyClassModel model = LazyClassModel.of(desc, () -> context.parse(bytes));

			byte[] result = manager.transformFor(model)
					.map(transform -> transform.andThen(DefaultConstructorStripper.INSTANCE))
					.map(transform -> context.transform(model.get(), transform))
					.orElse(bytes);

			transformed.put(name, result);
		});

		return transformed;
	}

	private static String cleanupDecompile(String decompiled) {
		String withoutThis = decompiled.replace("this.", "");
		int classStart = withoutThis.indexOf("class TestTarget {");

		if (classStart != -1) {
			return withoutThis.substring(classStart);
		} else {
			return withoutThis;
		}
	}

	private static void dumpBytes(Path path, byte[] bytes) {
		try {
			Files.deleteIfExists(path);
			Files.write(path, bytes, StandardOpenOption.CREATE);
			System.out.println("Class bytes dumped to " + path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
