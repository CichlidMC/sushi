package fish.cichlidmc.sushi.test.hooks;

import fish.cichlidmc.sushi.api.ref.IntRef;
import fish.cichlidmc.sushi.api.transform.infra.Cancellation;
import fish.cichlidmc.sushi.api.transform.infra.Operation;
import fish.cichlidmc.sushi.test.TestTarget;

public final class Hooks {
	static {
		System.out.println("Hooks loaded too early!");
		System.exit(1);
	}

	public static void inject() {
		System.out.println("h");
	}

	public static Cancellation<Integer> injectAndCancel() {
	    return Cancellation.none();
	}

	public static void injectWithLocal(int local) {
	}

	public static void injectWithMutableLocal(IntRef local) {
	}

	public static int modifyInt(int i) {
		return i;
	}

	public static int wrapGetInt(TestTarget target, boolean b, Operation<Integer> operation) {
		return operation.call(target, b);
	}

	public static void wrapDoThing(TestTarget target, int x, String s, Operation<Void> operation) {
		operation.call(target, x, s);
	}

	public static Object modifyObject(Object object) {
		return object;
	}

	public interface ThingDoer {
		default void doThing() {

		}
	}
}
