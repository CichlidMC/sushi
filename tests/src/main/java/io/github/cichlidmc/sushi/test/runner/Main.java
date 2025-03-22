package io.github.cichlidmc.sushi.test.runner;

import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.DirectoryResultSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Main {
	public static final Path INPUT = Paths.get("input");
	public static final Path TRANSFORMED = Paths.get("transformed");
	public static final Path OUTPUT = Paths.get("output");
	public static final Path KNOWN_GOOD = Paths.get("known_good");

	public static void main(String[] args) throws URISyntaxException, IOException {
		deleteRecursively(TRANSFORMED);
		deleteRecursively(OUTPUT);

		List<String> errors = new ArrayList<>();
		TransformerManager manager = getTransformers(errors);
		if (!errors.isEmpty()) {
			errors.forEach(System.out::println);
			System.exit(1);
		}

		forEachInput((input, transformed) -> {
			byte[] bytes = Files.readAllBytes(input);
			ClassReader reader = new ClassReader(bytes);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			manager.transform(node);

			ClassWriter writer = new ClassWriter(reader, 0);
			node.accept(writer);
			Files.createDirectories(transformed.getParent());
			Files.write(transformed, writer.toByteArray(), StandardOpenOption.CREATE);
		});

		Fernflower ff = new Fernflower(new DirectoryResultSaver(OUTPUT.toFile()), Map.of(), IFernflowerLogger.NO_OP);
		ff.addSource(TRANSFORMED.toFile());
		ff.decompileContext();

		errors.addAll(compare());
		if (!errors.isEmpty()) {
			errors.forEach(System.out::println);
			System.exit(1);
		}
	}

	private static List<String> compare() throws IOException {
		List<String> errors = new ArrayList<>();
		Files.walkFileTree(OUTPUT, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
				String relative = OUTPUT.relativize(file).toString();
				Path knownGood = KNOWN_GOOD.resolve(relative);
				if (!Files.exists(knownGood)) {
					errors.add("Missing known-good file for " + file);
					return FileVisitResult.CONTINUE;
				}

				String content = Files.readString(file);
				String expected = Files.readString(knownGood);
				if (!content.equals(expected)) {
					errors.add("Mismatch for " + file);
				}

				return FileVisitResult.CONTINUE;
			}
		});
		return errors;
	}

	private static void forEachInput(InputConsumer consumer) throws IOException {
		try (Stream<Path> stream = Files.list(INPUT)) {
			for (Iterator<Path> itr = stream.iterator(); itr.hasNext();) {
				Path jar = itr.next();
				try (FileSystem fs = FileSystems.newFileSystem(jar)) {
					Path root = fs.getRootDirectories().iterator().next();
					Files.walkFileTree(root, new SimpleFileVisitor<>() {
						@Override
						public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
							if (!file.toString().endsWith(".class"))
								return FileVisitResult.CONTINUE;

							String relative = root.relativize(file).toString();
							Path transformed = TRANSFORMED.resolve(relative);
							consumer.accept(file, transformed);
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
		}
	}

	private static TransformerManager getTransformers(List<String> errors) throws URISyntaxException, IOException {
		URL transformers = Main.class.getClassLoader().getResource("transformers");
		Objects.requireNonNull(transformers);
		Path path = Paths.get(transformers.toURI());

		TransformerManager.Builder builder = TransformerManager.builder();

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String relative = path.relativize(file).toString();
				if (!relative.endsWith(".sushi"))
					return FileVisitResult.CONTINUE;

				String name = relative.substring(0, relative.length() - ".sushi".length());
				Id id = new Id("tests", name);

				JsonValue json = TinyJson.parse(file);
				builder.parseAndRegister(id, json).ifPresent(
						error -> errors.add("Error registering transformer " + id + ": " + error)
				);

				return FileVisitResult.CONTINUE;
			}
		});

		return builder.build();
	}

	private static void deleteRecursively(Path path) throws IOException {
		if (!Files.exists(path))
			return;

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public interface InputConsumer {
		void accept(Path input, Path transformed) throws IOException;
	}
}
