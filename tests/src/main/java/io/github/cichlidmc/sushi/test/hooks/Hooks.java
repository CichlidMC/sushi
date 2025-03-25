package io.github.cichlidmc.sushi.test.hooks;

import io.github.cichlidmc.sushi.api.transform.Cancellation;

public final class Hooks {
	public static Cancellation<Void> simpleInjectHead() {
		System.out.println("Injected!");
		return Cancellation.none();
	}
}
