package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.InstructionComparisons;
import fish.cichlidmc.sushi.api.model.code.Point;

public interface RangedOperation {
	Point from();
	Point to();

	default boolean contains(Point point, InstructionComparisons instructions) {
		return instructions.rangeContains(this.from(), this.to(), point);
	}
}
