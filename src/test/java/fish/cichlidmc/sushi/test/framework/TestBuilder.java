package fish.cichlidmc.sushi.test.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
			TestExecutor.execute(this.base.source, this.processTransformers(), Optional.of(full), this.base.factory.metadata());
		}

		public void fail() {
			TestExecutor.execute(this.base.source, this.processTransformers(), Optional.empty(), this.base.factory.metadata());
		}

		private List<String> processTransformers() {
			return this.transformers.stream()
					.map(this.base.factory::expandDefinitions)
					.toList();
		}
	}
}
