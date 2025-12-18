package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;

import java.util.function.Consumer;
import java.util.function.Function;

public final class TestBuilder implements Transformable<TestBuilder> {
	private final String source;
	private final TestFactory factory;
	private final TransformerManager.Builder manager;

	private int nextIdNumber;

	public TestBuilder(String source, TestFactory factory) {
		this.source = source;
		this.factory = factory;
		this.manager = TransformerManager.builder();
		this.manager.addMetadata(factory.metadata());
	}

	@Override
	public TestBuilder transform(ConfiguredTransformer transformer) {
		this.manager.defaultPhase().registerOrThrow(transformer);
		return this;
	}

	@Override
	public TestBuilder transform(Transformer transformer) {
		return this.transform(new ConfiguredTransformer(this.nextId(), transformer));
	}

	@Override
	public TestBuilder transform(Function<Id, ConfiguredTransformer> factory) {
		return this.transform(factory.apply(this.nextId()));
	}

	public TestBuilder inPhase(Id id, Consumer<PhaseBuilder> consumer) {
		Phase.Builder builder = this.manager.definePhaseOrThrow(id);
		consumer.accept(new PhaseBuilder(builder));
		return this;
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
		TestExecutor.execute(this.source, this.manager.build(), result);
	}

	private Id nextId() {
		Id id = new Id("tests", String.valueOf(this.nextIdNumber));
		this.nextIdNumber++;
		return id;
	}

	public class PhaseBuilder implements Transformable<PhaseBuilder> {
		public final Phase.Builder builder;

		public PhaseBuilder(Phase.Builder builder) {
			this.builder = builder;
		}

		@Override
		public PhaseBuilder transform(ConfiguredTransformer transformer) {
			this.builder.registerOrThrow(transformer);
			return this;
		}

		@Override
		public PhaseBuilder transform(Transformer transformer) {
			return this.transform(new ConfiguredTransformer(TestBuilder.this.nextId(), transformer));
		}

		@Override
		public PhaseBuilder transform(Function<Id, ConfiguredTransformer> factory) {
			return this.transform(factory.apply(TestBuilder.this.nextId()));
		}
	}
}
