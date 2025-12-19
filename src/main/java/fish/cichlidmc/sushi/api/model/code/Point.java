package fish.cichlidmc.sushi.api.model.code;

/// A point somewhere in a method's instructions.
/// Points are anchored either before or after a single instruction.
public record Point(InstructionHolder<?> instruction, Offset offset) implements Comparable<Point> {
	@Override
	public int compareTo(Point that) {
		int byInstruction = this.instruction.compareTo(that.instruction);
		return byInstruction != 0 ? byInstruction : this.offset.compareTo(that.offset);
	}

	public static Point before(InstructionHolder<?> instruction) {
		return new Point(instruction, Offset.BEFORE);
	}

	public static Point after(InstructionHolder<?> instruction) {
		return new Point(instruction, Offset.AFTER);
	}
}
