package fish.cichlidmc.sushi.api.detail;

import fish.cichlidmc.sushi.impl.detail.DetailedExceptionHelper;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// An exception that has [Details], providing a large amount of information in a manageable format.
///
/// The [message][Throwable#getMessage()] of a DetailedException will never be null.
public sealed interface DetailedException {
	/// @return the [Details] of this exception. May be modified as this exception propagates upwards.
	Details details();

	/// A function that creates a [DetailedException] given a message and cause.
	@FunctionalInterface
	interface Factory<X extends Throwable & DetailedException> {
		X create(String message, @Nullable Throwable cause);
	}

	// I hate the Throwable hierarchy

	/// Base class for a checked [DetailedException].
	non-sealed abstract class Checked extends Exception implements DetailedException {
		private final DetailedExceptionHelper helper;

		protected Checked(String message) {
			this(message, (Throwable) null);
		}

		protected Checked(String message, Consumer<Details> initialDetails) {
			this(message, null, initialDetails);
		}

		protected Checked(String message, @Nullable Throwable cause) {
			this(message, cause, _ -> {});
		}

		protected Checked(String message, @Nullable Throwable cause, Consumer<Details> initialDetails) {
			super(DetailedExceptionHelper.HIDDEN_MESSAGE, cause);
			this.helper = new DetailedExceptionHelper(message);
			initialDetails.accept(this.details());
		}

		@Override
		public Details details() {
			return this.helper.details();
		}

		@Override
		public String getMessage() {
			return this.helper.buildMessage();
		}
	}

	/// Base class for an unchecked [DetailedException].
	non-sealed abstract class Unchecked extends RuntimeException implements DetailedException {
		private final DetailedExceptionHelper helper;

		protected Unchecked(String message) {
			this(message, (Throwable) null);
		}

		protected Unchecked(String message, Consumer<Details> initialDetails) {
			this(message, null, initialDetails);
		}

		protected Unchecked(String message, @Nullable Throwable cause) {
			this(message, cause, _ -> {});
		}

		protected Unchecked(String message, @Nullable Throwable cause, Consumer<Details> initialDetails) {
			super(DetailedExceptionHelper.HIDDEN_MESSAGE, cause);
			this.helper = new DetailedExceptionHelper(message);
			initialDetails.accept(this.details());
		}

		@Override
		public Details details() {
			return this.helper.details();
		}

		@Override
		public String getMessage() {
			return this.helper.buildMessage();
		}
	}
}
