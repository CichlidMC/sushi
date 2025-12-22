package fish.cichlidmc.sushi.impl.detail;

import fish.cichlidmc.sushi.api.detail.DetailedException;
import fish.cichlidmc.sushi.api.detail.Details;

/// An attempt to move as much impl code out of [DetailedException] as possible
public final class DetailedExceptionHelper {
	public static final String HIDDEN_MESSAGE = "This should never be visible! Please report this.";

	private final String baseMessage;
	private final DetailsImpl details;

	public DetailedExceptionHelper(String baseMessage) {
		this.baseMessage = baseMessage;
		this.details = new DetailsImpl();
	}

	public String buildMessage() {
		return this.details.buildMessage(this.baseMessage);
	}

	public Details details() {
		return this.details;
	}
}
