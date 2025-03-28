package io.github.cichlidmc.sushi.test.hooks;

import io.github.cichlidmc.sushi.api.transform.Cancellation;

public final class Hooks {
	static {
		System.out.println("hooks loaded!!!");
		System.exit(1);
	}

	public static Cancellation<Void> simpleInjectHead() {
		return Cancellation.none();
	}

	public static Cancellation<Void> simpleInjectHeadSpecific() {
		return Cancellation.none();
	}

	public static Cancellation<Void> simpleInjectHeadExplicitTarget() {
		return Cancellation.none();
	}

	public static Cancellation<Integer> implicitMultiTarget() {
		return Cancellation.none();
	}

	public static Cancellation<String> multiTransformHead() {
		return Cancellation.none();
	}

	public static Cancellation<String> multiTransformTail() {
		return Cancellation.none();
	}

	public static double modifyExpression(double value) {
		return value;
	}
}
