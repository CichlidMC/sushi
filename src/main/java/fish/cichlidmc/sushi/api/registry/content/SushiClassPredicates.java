package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [ClassPredicate] types provided by Sushi.
public final class SushiClassPredicates {
	public static final Id SINGLE = id("single");
	public static final Id ANY = id("any");
	public static final Id EVERYTHING = id("everything");

	private SushiClassPredicates() {
	}
}
