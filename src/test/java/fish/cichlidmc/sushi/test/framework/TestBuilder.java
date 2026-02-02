package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.sushi.test.framework.TestResult.Success.Invocation.Parameter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/// A builder for a test case that runs through Sushi.
/// @see TestFactory
public final class TestBuilder implements Transformable<TestBuilder> {
	private final String source;
	private final TestFactory factory;
	private final TransformerManager.Builder manager;

	private int nextIdNumber;

	@Nullable
	private TestResult result;

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

	/// Define a new phase of transformers.
	/// @param id the phase's ID
	/// @param consumer a consumer that will be invoked with the phase's [builder][PhaseBuilder]
	public TestBuilder inPhase(Id id, Consumer<PhaseBuilder> consumer) {
		Phase.Builder builder = this.manager.definePhaseOrThrow(id);
		consumer.accept(new PhaseBuilder(builder));
		return this;
	}

	/// Define the expected decompiled output for this test.
	public TestBuilder decompile(String output) {
		String full = this.factory.addToTemplate(output).trim();
		this.setResult(new TestResult.Success(full, Optional.empty()));
		return this;
	}

	/// Define an invocation that should be applied to the transformed output.
	/// @throws IllegalStateException if [decompile][#decompile(String)] hasn't been called or if [#fail()] has been.
	public TestBuilder invoke(String method, List<Parameter> params, @Nullable Object returned) {
		return this.invoke(method, params, returned, false);
	}

	/// [invoke][#invoke(String, List, Object)], but the invoked method is static.
	public TestBuilder invokeStatic(String method, List<Parameter> params, @Nullable Object returned) {
		return this.invoke(method, params, returned, true);
	}

	/// Define that this test is expected to fail with an unspecified error message, and then execute it.
	public void fail() {
		this.setResult(TestResult.Fail.EMPTY);
		this.execute();
	}

	/// Define that this test is expected to fail with the given message, and then execute it.
	public void fail(String message) {
		this.setResult(new TestResult.Fail(message.trim()));
		this.execute();
	}

	/// Execute this test.
	public void execute() {
		if (this.result == null) {
			throw new IllegalStateException("TestBuilder has no expected result");
		}

		TestExecutor.execute(this.source, this.manager.build(), this.result);
	}

	private Id nextId() {
		Id id = new Id("tests", String.valueOf(this.nextIdNumber));
		this.nextIdNumber++;
		return id;
	}

	private TestBuilder invoke(String method, List<Parameter> params, @Nullable Object returned, boolean isStatic) {
		this.result = switch (this.result) {
			case null -> throw new IllegalStateException("decompile() must be called before invoke()");
			case TestResult.Fail _ -> throw new IllegalStateException("Cannot invoke a failing test");
			case TestResult.Success success -> {
				if (success.invocation().isPresent()) {
					throw new IllegalStateException("An invocation has already been set");
				} else {
					TestResult.Success.Invocation invocation = new TestResult.Success.Invocation(method, params, returned, isStatic);
					yield new TestResult.Success(success.decompiled(), Optional.of(invocation));
				}
			}
		};

		return this;
	}

	private void setResult(TestResult result) {
		if (this.result != null) {
			throw new IllegalStateException("TestBuilder already has an expected result");
		}

		this.result = result;
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
