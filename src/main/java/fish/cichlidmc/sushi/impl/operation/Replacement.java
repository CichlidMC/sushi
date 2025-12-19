package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.registry.Id;

public record Replacement(Point from, Point to, CodeBlock replacement, Id owner) implements RangedOperation {
	boolean conflictsWith(RangedOperation that) {
		boolean containsStart = this.contains(that.from());
		boolean containsEnd = this.contains(that.to());
		// if either end is contained, that's a conflict.
		// note that an extraction containing a replacement is explicitly allowed.
		return containsStart || containsEnd;
	}

	boolean conflictsWith(Insertion insertion) {
		return this.contains(insertion.point());
	}
}
