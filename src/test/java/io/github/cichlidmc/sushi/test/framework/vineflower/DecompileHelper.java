package io.github.cichlidmc.sushi.test.framework.vineflower;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.util.Map;

public final class DecompileHelper {
	private final Map<String, Object> properties;

	public DecompileHelper(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String decompile(String className, byte[] bytes) {
		IContextSource input = new ByteArrayContextSource(className, bytes);
		ResultSaver results = new ResultSaver();

		Fernflower fernflower = new Fernflower(results, this.properties, IFernflowerLogger.NO_OP);
		fernflower.addSource(input);

		fernflower.decompileContext();
		return results.getContentOrThrow();
	}
}
