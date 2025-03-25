package io.github.cichlidmc.sushi.test.hooks;

import io.github.cichlidmc.sushi.api.transform.Cancellation;

public final class Hooks {
	public static Cancellation<Void> simpleInjectHead() {
		return Cancellation.none();
	}

	public static Cancellation<Void> simpleInjectHeadSpecific() {
		return Cancellation.none();
	}
}
