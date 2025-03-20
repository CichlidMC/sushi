package io.github.cichlidmc.sushi.api.transform;

/**
 * An exception that occurred while transforming a class.
 */
public class TransformException extends RuntimeException {
	public TransformException(String message) {
		super(message);
	}

	public TransformException(String message, Throwable cause) {
		super(message, cause);
	}
}
