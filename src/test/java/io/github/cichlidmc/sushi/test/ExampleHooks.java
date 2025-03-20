package io.github.cichlidmc.sushi.test;

public class ExampleHooks {
	public static void headHook(int x, Object gerald, boolean local) {
		System.out.println("Hooked at head of method! Value: " + x + ", local: " + local);
	}

	public static int redirectHashCode(Object gerald, boolean local) {
		return -5;
	}
}
