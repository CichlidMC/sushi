package fish.cichlidmc.sushi.impl.runtime;

/**
 * Runtime validation for {@code wrap_operation} transforms.
 */
public final class WrapOpValidation {
	private WrapOpValidation() {}

	public static void checkCount(Object[] args, int expectedSize) {
		if (args.length != expectedSize) {
			throw new RuntimeException(
					"Received invalid argument array after wrap: expected " + expectedSize + " element(s), got " + args.length
			);
		}
	}
}
