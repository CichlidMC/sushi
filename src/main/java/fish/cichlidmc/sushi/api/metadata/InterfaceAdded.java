package fish.cichlidmc.sushi.api.metadata;

import fish.cichlidmc.sushi.api.transformer.builtin.AddInterfaceTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to classes to indicate where added interfaces came from.
 * This interface is manged by {@link AddInterfaceTransformer}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceAdded {
	/**
	 * The interface that has been added to the class.
	 */
	Class<?> value();

	/**
	 * An array of the IDs of the transformers which added this interface.
	 */
	String[] by();
}
