package fish.cichlidmc.sushi.test.framework;

import org.jspecify.annotations.Nullable;

import java.util.List;
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
		/// @param params an array of method [Parameter]s
		/// @param returned the (nullable) expected return value
		/// @param isStatic if true, the method is expected to be static
		public record Invocation(String method, List<Parameter> params, @Nullable Object returned, boolean isStatic) {
			public Class<?>[] parameterTypes() {
				return this.params.stream().map(Parameter::clazz).toArray(Class[]::new);
			}

			public Object[] parameterValues() {
				return this.params.stream().map(Parameter::value).toArray();
			}

			/// A parameter for a method invocation.
			/// @param clazz the type of the parameter
			/// @param value the (nullable) value to pass in
			public record Parameter(Class<?> clazz, @Nullable Object value) {}
		}
	}
}
