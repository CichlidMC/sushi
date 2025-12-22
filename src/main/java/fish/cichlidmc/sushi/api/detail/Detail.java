package fish.cichlidmc.sushi.api.detail;

import fish.cichlidmc.sushi.impl.detail.DetailProviderImpl;

import java.util.function.Supplier;

/// A single detail belonging to a [DetailedException].
/// @param name the name of this detail, ex. `Target Class`
/// @param value the value of this detail, possibly multi-line
public record Detail(String name, String value) {
	/// Wraps a value in a [Supplier] and defers [Object#toString()] to the supplier's value.
	///
	/// This allows for easy lazy evaluation of details.
	public sealed interface Provider permits DetailProviderImpl {
		/// @return the result of calling [Object#toString()] on the wrapped [Supplier]'s value
		@Override
		String toString();

		static Provider of(Supplier<Object> supplier) {
			return new DetailProviderImpl(supplier);
		}
	}
}
