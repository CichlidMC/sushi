package fish.cichlidmc.sushi.api.model.code;

import java.lang.classfile.CodeBuilder;

/// A block of code that can be written to a builder.
@FunctionalInterface
public interface CodeBlock {
	/// A CodeBlock containing no code.
	CodeBlock EMPTY = builder -> {};

	void write(CodeBuilder builder);
}
