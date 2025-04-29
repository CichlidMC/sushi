package fish.cichlidmc.sushi.test.framework.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

// from the JavaCompiler javadoc
public final class SourceObject extends SimpleJavaFileObject {
	private final String source;

	public SourceObject(String name, String source) {
		super(uriOf(name), Kind.SOURCE);
		this.source = source;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return this.source;
	}

	private static URI uriOf(String source) {
		return URI.create("string:///" + source.replace('.', '/') + Kind.SOURCE.extension);
	}
}
