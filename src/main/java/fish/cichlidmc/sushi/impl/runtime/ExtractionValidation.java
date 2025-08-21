package fish.cichlidmc.sushi.impl.runtime;

/**
 * Runtime validation for extractions.
 */
public final class ExtractionValidation {
	private ExtractionValidation() {}

	public static void checkCount(Object[] args, int expectedSize) {
		if (args.length != expectedSize) {
			throw new RuntimeException(
					"Received invalid argument array after wrap: expected " + expectedSize + " element(s), got " + args.length
			);
		}
	}
}
