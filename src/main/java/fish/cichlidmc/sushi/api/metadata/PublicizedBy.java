package fish.cichlidmc.sushi.api.metadata;

import fish.cichlidmc.sushi.impl.transform.access.PublicizeClassTransform;
import fish.cichlidmc.sushi.impl.transform.access.PublicizeFieldTransform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class or field has been made public by one or more transformers.
 * Managed by:
 * <ul>
 *     <li>{@link PublicizeClassTransform}</li>
 *     <li>{@link PublicizeFieldTransform}</li>
 * </ul>
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicizedBy {
	String[] value();
}
