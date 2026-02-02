package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.Sushi;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class UnsafeHolder {
	public static final sun.misc.Unsafe INSTANCE = Sushi.make(() -> {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	});

	private UnsafeHolder() {}
}
