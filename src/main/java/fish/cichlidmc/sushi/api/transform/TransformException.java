package fish.cichlidmc.sushi.api.transform;

/**
 * An exception that occurred while transforming a class. May be thrown by transformers if an irrecoverable problem occurs.
 */
public class TransformException extends RuntimeException {
	public TransformException(String message) {
		super(message);
	}

	public TransformException(String message, Throwable cause) {
		super(message, cause);
	}
}
