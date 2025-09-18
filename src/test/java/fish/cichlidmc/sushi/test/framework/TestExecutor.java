package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.test.framework.compiler.FileManager;
import fish.cichlidmc.sushi.test.framework.compiler.SourceObject;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.impl.verifier.VerifierImpl;
import org.junit.jupiter.api.Assertions;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TestExecutor {
	public static void execute(String source, List<String> transformers, TestResult result, boolean metadata) {
		boolean executed = false;
		Optional<String> expectedOutput = result instanceof TestResult.Expect(String value) ? Optional.of(value) : Optional.empty();

		try {
			doExecute(source, transformers, expectedOutput, metadata);
			executed = true;
		} catch (RuntimeException e) {
			switch (result) {
				case TestResult.Expect ignored -> throw e;
				case TestResult.Exception(Optional<String> message) -> message.ifPresent(
						s -> Assertions.assertEquals(s, e.getMessage())
				);
			}
		}

		if (executed && result instanceof TestResult.Exception) {
			Assertions.fail("Test did not fail, but should've");
		}
	}

	private static void doExecute(String source, List<String> transformers, Optional<String> expectedOutput, boolean metadata) {
		Map<String, byte[]> output = compile(source);

		TransformerManager manager = prepareTransformers(transformers, metadata);
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

		transformed.forEach(TestExecutor::dumpBytes);
		Assertions.assertEquals(expectedOutput.get(), mainOutput);
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

	private static TransformerManager prepareTransformers(List<String> transformers, boolean metadata) {
		TransformerManager.Builder builder = TransformerManager.builder()
				.withValidation(Validation.runtime(MethodHandles.lookup()))
				.addMetadata(metadata);

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

			byte[] result = manager.transform(context, bytes, desc, DefaultConstructorStripper.INSTANCE).orElse(bytes);

			// bypass context.verify so we can provide a logger
			ClassModel reParsed = context.parse(result);
			Consumer<String> logger = System.out::println;
			List<VerifyError> errors = VerifierImpl.verify(reParsed, null);
			if (!errors.isEmpty()) {
				dumpBytes(name, result);
				RuntimeException exception = new RuntimeException("Transformed class fails validation");
				errors.forEach(exception::addSuppressed);
				throw exception;
			}

			transformed.put(name, result);
		});

		return transformed;
	}

	private static String cleanupDecompile(String decompiled) {
		StringBuilder result = new StringBuilder();

		boolean foundClassStart = false;
		for (String line : decompiled.split("\n")) {
			if (!foundClassStart) {
				foundClassStart = !line.isBlank() && !line.startsWith("package") && !line.startsWith("import");
			}

			if (foundClassStart) {
				String withoutThis = line.replace("this.", "");
				result.append(withoutThis).append('\n');
			}
		}

		return result.toString().trim();
	}

	private static void dumpBytes(String className, byte[] bytes) {
		Path path = Paths.get(className.substring(className.lastIndexOf('.') + 1) + ".class");

		try {
			Files.deleteIfExists(path);
			Files.write(path, bytes, StandardOpenOption.CREATE);
			System.out.println("Class bytes dumped to " + path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
