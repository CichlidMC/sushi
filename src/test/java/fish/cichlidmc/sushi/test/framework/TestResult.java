package fish.cichlidmc.sushi.test.framework;

import java.util.Optional;

public sealed interface TestResult {
	record Expect(String value) implements TestResult {
	}

	record Exception(Optional<String> message) implements TestResult {
		public static final Exception EMPTY = new Exception(Optional.empty());

		public Exception(String message) {
			this(Optional.of(message));
		}
	}
}
