package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.InstructionComparisons;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.registry.Id;

public record Replacement(Point from, Point to, CodeBlock replacement, Id owner) implements RangedOperation {
	boolean conflictsWith(RangedOperation that, InstructionComparisons instructions) {
		boolean containsStart = this.contains(that.from(), instructions);
		boolean containsEnd = this.contains(that.to(), instructions);
		// if either end is contained, that's a conflict.
		// note that an extraction containing a replacement is explicitly allowed.
		return containsStart || containsEnd;
	}

	boolean conflictsWith(Insertion insertion, InstructionComparisons instructions) {
		return this.contains(insertion.point(), instructions);
	}
}
