package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.transform.inject.Cancellation;
import fish.cichlidmc.sushi.test.hooks.Hooks;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class TestFactory {
	public static final String TEST_TARGET_CLASS_PACKAGE = "fish.cichlidmc.sushi.test";
	public static final String TEST_TARGET_CLASS_NAME = TEST_TARGET_CLASS_PACKAGE + ".TestTarget";

	public static final TestFactory ROOT = new TestFactory()
			.withDefinition("hooks", Hooks.class.getName())
			.withDefinition("cancellation", Cancellation.class.getName())
			.withDefinition("target", TEST_TARGET_CLASS_NAME);

	private final Map<String, String> definitions;
	private String classTemplate;

	public TestFactory() {
		// sort definitions by decreasing length so longer ones are filled first
		// allows cases like having both $var and $var_two
		this.definitions = new TreeMap<>(Comparator.comparingInt(String::length).reversed());
	}

	private TestFactory(Map<String, String> definitions, String classTemplate) {
		this();
		this.definitions.putAll(definitions);
		this.classTemplate = classTemplate;
	}

	public TestFactory fork() {
		return new TestFactory(this.definitions, this.classTemplate);
	}

	public TestFactory withDefinition(String placeholder, String value) {
		this.define(placeholder, this.expandDefinitions(value));
		return this;
	}

	public TestFactory withClassTemplate(String template) {
		this.classTemplate = template;
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

	public String addToTemplate(String content) {
		if (this.classTemplate == null) {
			throw new IllegalStateException("Class template is not defined");
		}

		return this.classTemplate.formatted(content);
	}

	public TestBuilder compile(String source) {
		String fullSource = String.format("""
				package %s;
				
				%s
				""", TEST_TARGET_CLASS_PACKAGE, this.addToTemplate(source)
		);

		return new TestBuilder(fullSource, this);
	}
}
