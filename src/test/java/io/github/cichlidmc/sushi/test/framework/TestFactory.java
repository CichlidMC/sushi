package io.github.cichlidmc.sushi.test.framework;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class TestFactory {
	public static final String TEST_TARGET_CLASS_PACKAGE = "io.github.cichlidmc.sushi.test";
	public static final String TEST_TARGET_CLASS_NAME = TEST_TARGET_CLASS_PACKAGE + ".TestTarget";
	public static final String TEST_TARGET_CLASS_BASE = """
			class TestTarget {
			%s
			
				void noop() {
				}
			}
			""";

	public static final TestFactory ROOT = new TestFactory()
			.withDefinition("target", TEST_TARGET_CLASS_NAME);

	private final Map<String, String> definitions;

	public TestFactory() {
		// sort definitions by decreasing length so longer ones are filled first
		// allows cases like having both $var and $var_two
		this.definitions = new TreeMap<>(Comparator.comparingInt(String::length).reversed());
	}

	private TestFactory(Map<String, String> definitions) {
		this();
		this.definitions.putAll(definitions);
	}

	public TestFactory fork() {
		return new TestFactory(this.definitions);
	}

	public TestFactory withDefinition(String placeholder, String value) {
		this.define(placeholder, value);
		return this;
	}

	public void define(String placeholder, String value) {
		String key = '$' + placeholder;

		if (this.definitions.containsKey(key)) {
			String existing = this.definitions.get(key);
			throw new IllegalArgumentException(placeholder + " is already defined as " + existing);
		}

		this.definitions.put(key, value);
	}

	public String expandDefinitions(String input) {
		for (Map.Entry<String, String> entry : this.definitions.entrySet()) {
			input = input.replace(entry.getKey(), entry.getValue());
		}
		return input;
	}

	public TestBuilder compile(String source) {
		String fullSource = String.format("""
				package %s;
				
				%s
				""", TEST_TARGET_CLASS_PACKAGE, TEST_TARGET_CLASS_BASE.formatted(source)
		);

		return new TestBuilder(fullSource, this);
	}
}
