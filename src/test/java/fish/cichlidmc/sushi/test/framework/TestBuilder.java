package fish.cichlidmc.sushi.test.framework;

import java.util.ArrayList;
import java.util.List;

public record TestBuilder(String source, TestFactory factory) {
	public WithTransformers transform(String transformer) {
		return new WithTransformers(this, List.of(transformer));
	}

	public record WithTransformers(TestBuilder base, List<String> transformers) {
		public WithTransformers transform(String transformer) {
			List<String> transformers = new ArrayList<>(this.transformers);
			transformers.add(transformer);
			return new WithTransformers(this.base, transformers);
		}

		public void expect(String output) {
			String full = this.base.factory.addToTemplate(output).trim();
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
			TestExecutor.execute(this.base.source, this.processTransformers(), result, this.base.factory.metadata());
		}

		private List<String> processTransformers() {
			return this.transformers.stream()
					.map(this.base.factory::expandDefinitions)
					.toList();
		}
	}
}
