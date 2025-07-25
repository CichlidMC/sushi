package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.util.Id;
import org.glavo.classfile.MethodBuilder;

import java.util.function.Consumer;

public interface UniqueMethodGenerator {
	void next(Consumer<MethodBuilder> builder);

	private static String sanitizeId(Id id) {
		String sanitizedPath = id.path.replace('/', '$').replace('.', '$');
		return id.namespace + "$" + sanitizedPath;
	}
}
