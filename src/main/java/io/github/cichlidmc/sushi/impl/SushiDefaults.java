package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.impl.target.ClassArrayTarget;

public class SushiDefaults {
	public static void register() {
		registerTargets();
	}

	private static void registerTargets() {
		ClassTarget.REGISTRY.register(id("single"), ClassArrayTarget.MAP_CODEC);
	}

	private static Id id(String path) {
		return new Id(Id.BUILT_IN_NAMESPACE, path);
	}
}
