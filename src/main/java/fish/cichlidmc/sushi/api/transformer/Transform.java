package fish.cichlidmc.sushi.api.transformer;

/**
 * A single class-transforming operation.
 */
@FunctionalInterface
public interface Transform {
	/**
	 * Apply this transform.
	 * @throws TransformException if something goes wrong while transforming the target class
	 */
	void apply(TransformContext context) throws TransformException;
}
