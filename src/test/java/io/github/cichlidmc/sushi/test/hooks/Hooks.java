package io.github.cichlidmc.sushi.test.hooks;

import io.github.cichlidmc.sushi.api.transform.inject.Cancellation;
import io.github.cichlidmc.sushi.api.transform.wrap_op.Operation;

public final class Hooks {
	static {
		System.out.println("Hooks loaded too early!");
		System.exit(1);
	}

	public static Cancellation<Void> inject() {
		return Cancellation.none();
	}

	public static int modifyInt(int i) {
		return i;
	}

	public static int wrapGetInt(boolean b, Operation<Integer> operation) {
		return operation.call(b);
	}

	public static void wrapDoThing(int x, String s, Operation<Void> operation) {
		operation.call(x);
	}

	public static Object modifyObject(Object object) {
		return object;
	}
}
