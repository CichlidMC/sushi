package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;

/// A point somewhere in a method's instructions.
/// Points are anchored either before or after a single instruction.
public record Point(InstructionHolder<?> instruction, Offset offset) implements Comparable<Point> {
	@Override
	public int compareTo(Point that) {
		int byInstruction = this.instruction.compareTo(that.instruction);
		return byInstruction != 0 ? byInstruction : this.offset.compareTo(that.offset);
	}

	// IDEA complains that this never returns 0, but that's correct in this case
	@SuppressWarnings("ComparatorMethodParameterNotUsed")
	public int compareTo(InstructionHolder<?> instruction) {
		int byInstruction = this.instruction.compareTo(instruction);
		if (byInstruction != 0) {
			return byInstruction;
		}

		return switch (this.offset) {
			case BEFORE -> -1;
			case AFTER -> 1;
		};
	}

	public static Point before(InstructionHolder<?> instruction) {
		return new Point(instruction, Offset.BEFORE);
	}

	public static Point after(InstructionHolder<?> instruction) {
		return new Point(instruction, Offset.AFTER);
	}
}
