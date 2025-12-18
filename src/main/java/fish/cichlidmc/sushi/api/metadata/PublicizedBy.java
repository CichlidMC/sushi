package fish.cichlidmc.sushi.api.metadata;

import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeFieldTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Indicates that a class or field has been made public by one or more transformers.
/// Managed by:
/// - [PublicizeClassTransformer]
/// - [PublicizeFieldTransformer]
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicizedBy {
	String[] value();
}
