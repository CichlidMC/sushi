package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection.Timing;
import fish.cichlidmc.sushi.api.registry.Id;

import java.lang.constant.MethodTypeDesc;

public record Extraction(Point from, Point to, String name, MethodTypeDesc desc, CodeBlock block, Id owner, Timing timing) implements RangedOperation {
	boolean conflictsWith(Extraction other) {
		boolean containsStart = this.contains(other.from);
		boolean containsEnd = this.contains(other.to);
		// contains neither: allowed
		// contains one: conflict
		// contains both: allowed
		return containsStart ^ containsEnd;
	}
}
