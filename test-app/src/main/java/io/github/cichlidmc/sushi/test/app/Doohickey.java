package io.github.cichlidmc.sushi.test.app;

public final class Doohickey {
	private final int i = 5;

	public void doStuff() {
		System.out.println("doStuff()V");
	}

	public double doStuff(int x) {
		System.out.println("doStuff(I)V");
		return x * 1.5;
	}
}
