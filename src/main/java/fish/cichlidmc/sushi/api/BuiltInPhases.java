package fish.cichlidmc.sushi.api;

/**
 * This class stores constant values for phases used by Sushi's built-in transform types.
 * This is provided as API mostly for documentation. In general, phases used by new transform
 * types should be unique. {@link #DEFAULT} is an outlier, and is a reasonable default value for
 * non-destructive transforms.
 */
public final class BuiltInPhases {
	/**
	 * The default phase. Used when there's no reason to change it.
	 */
	public static final int DEFAULT = 0;

	/**
	 * Phase for {@code wrap_operation}. Applies much later, as
	 * the process is destructive to many other transforms.
	 */
	public static final int WRAP_OPERATION = 1_000_000;

	/**
	 * Phase for {@code wrap_method}. Applies last-minute, since
	 * it's catastrophic for any transform that applies after.
	 */
	public static final int WRAP_METHOD = 1_000_000_000;

	private BuiltInPhases() {}
}
