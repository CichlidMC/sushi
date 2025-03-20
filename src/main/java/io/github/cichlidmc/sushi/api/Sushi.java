package io.github.cichlidmc.sushi.api;

import io.github.cichlidmc.sushi.impl.SushiDefaults;

/**
 * Entrypoint for Sushi. Should be called on startup to initialize.
 */
public class Sushi {
	public static void init() {
		SushiDefaults.register();
	}
}
