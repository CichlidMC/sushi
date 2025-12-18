package fish.cichlidmc.sushi.api.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotation added to classes to indicate what Sushi transformers have been applied to it.
/// This annotation is managed by Sushi itself.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransformedBy {
	/// An array of the IDs of the transformers that have been applied.
	String[] value();
}
