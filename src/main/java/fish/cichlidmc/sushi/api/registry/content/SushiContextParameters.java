package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/// All [ContextParameter] types provided by Sushi.
public final class SushiContextParameters {
	public static final Id IMMUTABLE_LOCAL = id("local/immutable");
	public static final Id MUTABLE_LOCAL = id("local/mutable");
	public static final Id SHARE = id("share");

	private SushiContextParameters() {
	}
}
