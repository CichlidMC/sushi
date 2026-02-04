package fish.cichlidmc.sushi.test.infra;

import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;

/// Mostly a dummy class, exists just so Hooks can reference it
public final class TestTarget {
	public static final ClassDesc DESC = ClassDescs.of(TestTarget.class);
	public static final String PACKAGE = TestTarget.class.getPackageName();
	public static final String NAME = TestTarget.class.getSimpleName();

	void test() {
		record InaccessibleType(String s) {}
	}

	record InnerType(int x) {}
}
