package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.InstructionComparisons;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.util.Id;
import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public record Extraction(
		Point from, Point to,
		String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> init,
		CodeBlock header, CodeBlock footer, CodeBlock replacement,
		Id owner
) implements RangedOperation {
	boolean conflictsWith(Extraction other, InstructionComparisons instructions) {
		boolean containsStart = this.contains(other.from, instructions);
		boolean containsEnd = this.contains(other.to, instructions);
		// contains neither: allowed
		// contains one: conflict
		// contains both: allowed
		return containsStart ^ containsEnd;
	}
}
