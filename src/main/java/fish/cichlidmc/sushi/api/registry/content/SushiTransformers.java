package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.Transformer;

import static fish.cichlidmc.sushi.api.Sushi.id;

/**
 * All {@link Transformer} types Sushi provides by default.
 */
public final class SushiTransformers {
	public static final Id INJECT = id("inject");
	public static final Id MODIFY_EXPRESSION = id("modify_expression");
	public static final Id WRAP_OPERATION = id("wrap_operation");
	public static final Id ADD_INTERFACE = id("add_interface");
	public static final Id PUBLICIZE_CLASS = id("publicize/class");
	public static final Id PUBLICIZE_FIELD = id("publicize/field");

	private SushiTransformers() {
	}
}
