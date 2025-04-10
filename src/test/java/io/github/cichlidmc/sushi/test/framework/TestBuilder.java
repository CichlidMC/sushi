package io.github.cichlidmc.sushi.test.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
			String indented = Arrays.stream(output.split("\n"))
					.map(s -> '\t' + s)
					.collect(Collectors.joining("\n"));

			String full = this.base.factory.addToTemplate(indented);

			TestExecutor.execute(this.base.source, this.processTransformers(), Optional.of(full));
		}

		public void fail() {
			TestExecutor.execute(this.base.source, this.processTransformers(), Optional.empty());
		}

		private List<String> processTransformers() {
			return this.transformers.stream()
					.map(this.base.factory::expandDefinitions)
					.toList();
		}
	}
}
