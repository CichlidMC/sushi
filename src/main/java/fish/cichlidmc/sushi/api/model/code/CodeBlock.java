package fish.cichlidmc.sushi.api.model.code;

import org.glavo.classfile.CodeBuilder;

/**
 * A block of code that can be written to a builder.
 */
@FunctionalInterface
public interface CodeBlock {
	void write(CodeBuilder builder);
}
