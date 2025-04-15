package io.github.cichlidmc.sushi.test.framework.vineflower;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.util.Map;

public final class DecompileHelper {
	private final Map<String, Object> properties;

	public DecompileHelper(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, String> decompile(Map<String, byte[]> inputs) {
		ResultSaver results = new ResultSaver();
		Fernflower fernflower = new Fernflower(results, this.properties, IFernflowerLogger.NO_OP);
		inputs.forEach((name, bytes) -> fernflower.addSource(new ByteArrayContextSource(name, bytes)));

		fernflower.decompileContext();
		return results.results;
	}
}
