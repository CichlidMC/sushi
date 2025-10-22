package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transform.Transform;

import static fish.cichlidmc.sushi.api.Sushi.id;

/**
 * All {@link Transform} types Sushi provides by default.
 */
public final class SushiTransforms {
	public static final Id INJECT = id("inject");
	public static final Id MODIFY_EXPRESSION = id("modify_expression");
	public static final Id WRAP_OPERATION = id("wrap_operation");
	public static final Id ADD_INTERFACE = id("add_interface");
	public static final Id SLICED = id("sliced");
	public static final Id PUBLICIZE_CLASS = id("publicize/class");
	public static final Id PUBLICIZE_FIELD = id("publicize/field");

	private SushiTransforms() {
	}
}
