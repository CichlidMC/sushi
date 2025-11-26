package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TestBuilder {
	private final String source;
	private final TestFactory factory;
	private final List<ConfiguredTransformer> transformers;

	private int nextIdNumber;

	public TestBuilder(String source, TestFactory factory) {
		this.source = source;
		this.factory = factory;
		this.transformers = new ArrayList<>();
	}

	public TestBuilder transform(ConfiguredTransformer transformer) {
		this.transformers.add(transformer);
		return this;
	}

	public TestBuilder transform(Transformer transformer) {
		return this.transform(new ConfiguredTransformer(this.nextId(), transformer));
	}

	public TestBuilder transform(Function<Id, ConfiguredTransformer> factory) {
		return this.transform(factory.apply(this.nextId()));
	}

	public void expect(String output) {
		String full = this.factory.addToTemplate(output).trim();
		TestResult result = new TestResult.Expect(full);
		this.execute(result);
	}

	public void fail() {
		this.execute(TestResult.Exception.EMPTY);
	}

	public void fail(String message) {
		this.execute(new TestResult.Exception(message.trim()));
	}

	private void execute(TestResult result) {
		TestExecutor.execute(this.source, this.transformers, result, this.factory.metadata());
	}

	private Id nextId() {
		Id id = new Id("tests", String.valueOf(this.nextIdNumber));
		this.nextIdNumber++;
		return id;
	}
}
