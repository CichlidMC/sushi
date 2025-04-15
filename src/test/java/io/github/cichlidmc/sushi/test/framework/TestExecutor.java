package io.github.cichlidmc.sushi.test.framework;

import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.test.framework.compiler.FileManager;
import io.github.cichlidmc.sushi.test.framework.compiler.SourceObject;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

		// dumpBytes(Paths.get("Output.class"), transformed);
		Map<String, String> decompiled = TestUtils.DECOMPILER.decompile(transformed);

		if (expectedOutput.isEmpty())
			return;

		String mainOutput = decompiled.values().stream()
				.filter(s -> s.contains("class TestTarget {"))
				.findFirst()
				.map(TestExecutor::cleanupDecompile)
				.orElseThrow();

		Assertions.assertEquals(expectedOutput.get(), mainOutput);
	}

	private static Map<String, byte[]> compile(String source) {
		StandardJavaFileManager standardManager = TestUtils.COMPILER.getStandardFileManager(null, null, null);
		FileManager manager = new FileManager(standardManager);

		List<JavaFileObject> input = List.of(new SourceObject(TestFactory.TEST_TARGET_CLASS_NAME, source));
		List<String> options = List.of("-g"); // include debugging info, like LVT names
		JavaCompiler.CompilationTask task = TestUtils.COMPILER.getTask(null, manager, null, options, null, input);
		if (!task.call()) {
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
			ClassReader reader = new ClassReader(bytes);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			manager.transform(node, null);

			ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			transformed.put(name, writer.toByteArray());
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
