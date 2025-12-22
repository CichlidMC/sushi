package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.detail.DetailedException;
import fish.cichlidmc.sushi.api.detail.Details;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// An exception that occurred while transforming a class.
/// May be thrown during the transformation process if something goes wrong.
///
/// In addition, a TransformException is also a [DetailedException], which
/// is how most information about what went wrong will be provided.
public class TransformException extends DetailedException.Unchecked {
	public TransformException(String message) {
		super(message);
	}

	public TransformException(String message, Consumer<Details> initialDetails) {
		super(message, initialDetails);
	}

	public TransformException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	public TransformException(String message, @Nullable Throwable cause, Consumer<Details> initialDetails) {
		super(message, cause, initialDetails);
	}
}
