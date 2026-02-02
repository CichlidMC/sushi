package fish.cichlidmc.sushi.test.framework;

public final class TestClassLoader extends ClassLoader {
	public TestClassLoader() {
		super(Thread.currentThread().getContextClassLoader());
	}

	public void defineClass(String name, byte[] bytes) {
		this.defineClass(name, bytes, 0, bytes.length);
	}
}
