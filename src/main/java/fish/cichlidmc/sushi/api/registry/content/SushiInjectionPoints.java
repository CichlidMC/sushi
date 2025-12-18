package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [InjectionPoint] types provided by Sushi.
public final class SushiInjectionPoints {
	public static final Id HEAD = id("head");
	public static final Id TAIL = id("tail");
	public static final Id RETURN = id("return");
	public static final Id EXPRESSION = id("expression");

	private SushiInjectionPoints() {
	}
}
