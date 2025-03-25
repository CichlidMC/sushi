package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.CodecResult;
import io.github.cichlidmc.tinycodecs.Codecs;

/**
 * A method name and a class name.
 */
public final class MethodInClass {
	public static final Codec<MethodInClass> CODEC = Codecs.STRING.flatXmap(MethodInClass::parse, MethodInClass::toString);

	/**
	 * The internal name of the class holding this method, ex. "java.lang.Object"
	 */
	public final String className;
	/**
	 * The name of this method, ex. "doStuff"
	 */
	public final String name;

	private MethodInClass(String className, String name) {
		this.className = className;
		this.name = name;
	}

	@Override
	public String toString() {
		return this.className + '.' + this.name;
	}

	private static CodecResult<MethodInClass> parse(String string) {
		int lastDot = string.lastIndexOf('.');
		if (lastDot == -1 || lastDot == string.length() - 1) {
			return CodecResult.error("Invalid method: should be formatted as 'my.class.name.methodName'");
		}
		String className = string.substring(0, lastDot);
		String name = string.substring(lastDot + 1);
		return CodecResult.success(new MethodInClass(className, name));
	}
}
