package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.transform.infra.Operation;
import org.glavo.classfile.CodeBuilder;

/**
 * A variant of a {@link CodeBlock} that is used for extractions.
 */
public interface ExtractionCodeBlock {
	/**
	 * Write code to the given builder.
	 * @param builder a {@link CodeBuilder} that writes code to the original, outer method
	 * @param operation a {@link CodeBlock} that will push an {@link Operation} to the top of the stack which, when invoked,
	 *                will invoke the original method.
	 */
	void write(CodeBuilder builder, CodeBlock operation);
}
