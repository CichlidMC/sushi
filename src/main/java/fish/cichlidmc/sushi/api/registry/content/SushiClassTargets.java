package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.ClassTarget;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [ClassTarget] types provided by Sushi.
public final class SushiClassTargets {
	public static final Id SINGLE_CLASS = id("single_class");
	public static final Id UNION = id("union");
	public static final Id EVERYTHING = id("everything");

	private SushiClassTargets() {
	}
}
