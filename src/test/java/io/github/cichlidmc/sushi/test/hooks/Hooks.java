package io.github.cichlidmc.sushi.test.hooks;

import io.github.cichlidmc.sushi.api.transform.inject.Cancellation;

public final class Hooks {
	static {
		System.out.println("Hooks loaded too early!");
		System.exit(1);
	}

	public static Cancellation<Void> inject() {
		return Cancellation.none();
	}
}
