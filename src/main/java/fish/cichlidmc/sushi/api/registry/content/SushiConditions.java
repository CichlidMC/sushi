package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [Condition] types provided by Sushi.
public final class SushiConditions {
	public static final Id ALL = id("all");
	public static final Id ANY = id("any");
	public static final Id NOT = id("not");
	public static final Id TRANSFORMER_PRESENT = id("transformer_present");

	private SushiConditions() {
	}
}
