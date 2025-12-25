package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.match.inject.PointSelector;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [PointSelector] types provided by Sushi.
public final class SushiPointSelectors {
	public static final Id HEAD = id("head");
	public static final Id TAIL = id("tail");
	public static final Id RETURN = id("return");
	public static final Id EXPRESSION = id("expression");

	private SushiPointSelectors() {
	}
}
