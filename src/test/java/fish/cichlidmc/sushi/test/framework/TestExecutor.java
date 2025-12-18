package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.test.framework.compiler.FileManager;
import fish.cichlidmc.sushi.test.framework.compiler.SourceObject;
import fish.cichlidmc.sushi.test.infra.TestTarget;
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
	private static final boolean logVerification = false;
	private static final RequirementInterpreters requirementInterpreters = RequirementInterpreters.forRuntime(MethodHandles.lookup());

	public static void execute(String source, TransformerManager manager, TestResult result) {
		boolean executed = false;
		Optional<String> expectedOutput = result instanceof TestResult.Expect(String value) ? Optional.of(value) : Optional.empty();

		try {
			doExecute(source, manager, expectedOutput);
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

	private static void doExecute(String source, TransformerManager manager, Optional<String> expectedOutput) {
		Map<String, byte[]> output = compile(source);
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

		List<JavaFileObject> input = List.of(new SourceObject(TestTarget.NAME, source));
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

	private static Map<String, byte[]> transform(TransformerManager manager, Map<String, byte[]> input) {
		Map<String, byte[]> transformed = new HashMap<>();

		input.forEach((name, bytes) -> {
			ClassDesc desc = ClassDesc.of(name);
			ClassFile context = ClassFile.of();

			Optional<TransformResult> result = manager.transform(context, bytes, desc, ConstructorStripper.INSTANCE);
			if (result.isEmpty()) {
				transformed.put(name, bytes);
				return;
			}

			checkRequirements(result.get().requirements());
			byte[] newBytes = result.get().bytes();

			// bypass context.verify so we can provide a logger
			ClassModel reParsed = context.parse(newBytes);
			Consumer<String> logger = logVerification ? System.out::println : null;
			List<VerifyError> errors = VerifierImpl.verify(reParsed, logger);
			if (!errors.isEmpty()) {
				dumpBytes(name, newBytes);
				RuntimeException exception = new RuntimeException("Transformed class fails validation");
				errors.forEach(exception::addSuppressed);
				throw exception;
			}

			transformed.put(name, newBytes);
		});

		return transformed;
	}

	private static void checkRequirements(Requirements requirements) {
		List<Requirements.Problem> problems = requirements.check(requirementInterpreters);
		if (problems.isEmpty())
			return;

		RuntimeException exception = new RuntimeException("One or more requirements are unmet");

		for (Requirements.Problem problem : problems) {
			exception.addSuppressed(problem.exception());
		}

		throw exception;
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
