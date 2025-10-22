package fish.cichlidmc.sushi.api.registry.content;

import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;

import static fish.cichlidmc.sushi.api.Sushi.id;

/**
 * All {@link ContextParameter} types provided by Sushi.
 */
public final class SushiContextParameters {
	public static final Id LOCAL_SLOT = id("local/slot");
	public static final Id SHARE = id("share");

	private SushiContextParameters() {
	}
}
