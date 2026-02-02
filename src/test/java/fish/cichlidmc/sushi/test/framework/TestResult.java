package fish.cichlidmc.sushi.test.framework;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/// The expected result of a unit test.
public sealed interface TestResult {
	/// A result indication a failure by exception.
	/// @param message the (optional) expected error message
	record Fail(Optional<String> message) implements TestResult {
		public static final Fail EMPTY = new Fail(Optional.empty());

		public Fail(String message) {
			this(Optional.of(message));
		}
	}

	/// A result indicating a successful test.
	/// @param decompiled the expected decompiled output
	/// @param invocation an optional [Invocation] to apply to the transformed class
	record Success(String decompiled, Optional<Invocation> invocation) implements TestResult {
		/// A method invocation.
		/// @param method the name of the method to invoke
		/// @param params an array of (nullable) method parameters
		/// @param returned the (nullable) expected return value
		/// @param isStatic if true, the method is expected to be static
		public record Invocation(String method, @Nullable Object[] params, @Nullable Object returned, boolean isStatic) {}
	}
}
