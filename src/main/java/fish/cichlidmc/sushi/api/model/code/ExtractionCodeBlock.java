package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.transformer.infra.Operation;

import java.lang.classfile.CodeBuilder;

/// A variant of a [CodeBlock] that is used for extractions.
public interface ExtractionCodeBlock {
	/// Write code to the given builder.
	/// @param builder a [CodeBuilder] that writes code to the original, outer method
	/// @param operation a [CodeBlock] that will push an [Operation] to the top of
	/// 				the stack which, when invoked, will invoke the original method.
	void write(CodeBuilder builder, CodeBlock operation);
}
