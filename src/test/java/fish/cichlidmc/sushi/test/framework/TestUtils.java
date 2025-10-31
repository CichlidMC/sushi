package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.test.framework.vineflower.DecompileHelper;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.HashMap;
import java.util.Map;

public final class TestUtils {
	public static final JavaCompiler COMPILER = Sushi.make(() -> {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new RuntimeException("Current JVM does not provide JavaCompiler");
		}
		return compiler;
	});

	public static final DecompileHelper DECOMPILER = new DecompileHelper(Sushi.make(() -> {
		Map<String, Object> properties = new HashMap<>(IFernflowerPreferences.DEFAULTS);
		properties.put(IFernflowerPreferences.INDENT_STRING, "\t");
		return properties;
	}));
}
