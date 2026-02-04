package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [Requirement] types Sushi provides by default.
public final class SushiRequirements {
	public static final Id CLASS = id("class");
	public static final Id METHOD = id("method");
	public static final Id FIELD = id("field");
	public static final Id FLAGS = id("flags");
	public static final Id FULLY_DEFINED = id("fully_defined");
	public static final Id INHERITANCE = id("inheritance");

	private SushiRequirements() {
	}
}
