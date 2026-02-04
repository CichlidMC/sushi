package fish.cichlidmc.sushi.test.infra;

import fish.cichlidmc.sushi.api.ref.DoubleRef;
import fish.cichlidmc.sushi.api.ref.IntRef;
import fish.cichlidmc.sushi.api.ref.ShortRef;
import fish.cichlidmc.sushi.api.transformer.infra.Cancellation;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public final class Hooks {
	public static final ClassDesc DESC = ClassDescs.of(Hooks.class);

	public static void inject() {
		System.out.println("h");
	}

	public static Cancellation<Integer> injectAndCancel() {
	    return Cancellation.none();
	}

	public static void injectWithLocal(int local) {
	}

	public static void injectWithLocal(String s) {
	}

	public static void injectWithLocal(TestTarget self) {
	}

	public static void injectWithLocal(boolean bl) {
	}

	public static void injectWithLocals(int x, boolean bl) {
	}

	public static int wrapWithShare(TestTarget self, boolean b, Operation<Integer> original, ShortRef shared) {
		return original.call(self, b);
	}

	public static void injectWithShare(ShortRef shared) {
	}

	public static void injectWithMutableLocal(IntRef local) {
	}

	public static int modifyInt(int i) {
		return i;
	}

	public static int[] modifyIntArray(int[] ints) {
		return ints;
	}

	public static int modifyIntWithLocal(int i, byte b) {
		return i;
	}

	public static int wrapGetInt(TestTarget target, boolean b, Operation<Integer> operation) {
		return operation.call(target, b);
	}

	public static int wrapGetIntWithLocal(TestTarget target, boolean b, Operation<Integer> operation, DoubleRef d) {
		return operation.call(target, b);
	}

	public static int wrapGetIntWithLocal(TestTarget target, boolean b, Operation<Integer> operation, short s) {
		return operation.call(target, b);
	}

	public static void wrapDoThing(TestTarget target, int x, String s, Operation<Void> operation) {
		operation.call(target, x, s);
	}

	public static StringBuilder wrapConstruct(Operation<StringBuilder> operation) {
		return operation.call();
	}

	public static Object modifyObject(Object object) {
		return object;
	}

	public static void wrapTrivialMethod(TestTarget target, Operation<Void> operation) {
		operation.call(target);
	}

	public static int wrapGetIntMethod(TestTarget target, boolean bl, Operation<Integer> operation) {
		return operation.call(target, bl);
	}

	public static int wrapGetIntStaticMethod(boolean bl, Operation<Integer> operation) {
		return operation.call(bl);
	}

	public static int multiWrap(TestTarget target, boolean bl, Object o, Operation<Integer> operation) {
		return operation.call(target, bl, o);
	}

	public static String wrapWithCoercion(Object inaccessibleType, Operation<String> operation) {
		return operation.call(inaccessibleType) + "!";
	}

	public static int wrapArrayWithCoercion(TestTarget target, Record[] array, Operation<Integer> operation) {
		return operation.call(target, array) + 1;
	}

	public static Record modifyWithCoercion(Record inaccessibleType) {
		return inaccessibleType;
	}

	public static Object wrapMethodWithCoerce(TestTarget target, int x, Operation<Object> operation) {
		return operation.call(target, x) + "!!!";
	}
}
