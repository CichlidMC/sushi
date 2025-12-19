package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.Point;

public interface RangedOperation {
	Point from();
	Point to();

	default boolean contains(Point point) {
		return point.compareTo(this.from()) > 0 && point.compareTo(this.to()) < 0;
	}
}
