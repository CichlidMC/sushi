package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [ExpressionSelector] types provided by Sushi.
public final class SushiExpressionSelectors {
	public static final Id INVOKE = id("invoke");
	public static final Id NEW = id("new");
	public static final Id CONSTRUCT = id("construct");

	private SushiExpressionSelectors() {
	}
}
