package io.github.cichlidmc.sushi.test.framework.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.util.ArrayList;
import java.util.List;

public final class FileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	public final List<OutputClassFileObject> outputs = new ArrayList<>();

	public FileManager(StandardJavaFileManager fileManager) {
		super(fileManager);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
		if (kind != JavaFileObject.Kind.CLASS) {
			throw new UnsupportedOperationException();
		}

		OutputClassFileObject object = new OutputClassFileObject(className);
		this.outputs.add(object);
		return object;
	}
}
