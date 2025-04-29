package fish.cichlidmc.sushi.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation added to classes that have been transformed by Sushi to document the changes that have been made.
 * Each value starts with a transformer's ID, followed by a description of what that transformer does.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SushiMetadata {
	String[] value();
}
