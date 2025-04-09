package io.github.cichlidmc.sushi.test.framework.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public final class OutputClassFileObject extends SimpleJavaFileObject {
	public final String className;
	public final ByteArrayOutputStream bytes;

	public OutputClassFileObject(String className) {
		super(uriOf(className), Kind.CLASS);
		this.className = className;
		this.bytes = new ByteArrayOutputStream();
	}

	@Override
	public OutputStream openOutputStream() {
		return this.bytes;
	}

	private static URI uriOf(String source) {
		return URI.create("string:///" + source.replace('.', '/') + Kind.CLASS.extension);
	}
}
