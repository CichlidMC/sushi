package fish.cichlidmc.sushi.api.model.code;

import org.glavo.classfile.CodeElement;

/// Comparison methods for instructions and points attached to them.
///
/// All methods here will throw [IllegalArgumentException] if a given instruction is not associated with this object.
public interface InstructionComparisons {
	int indexOf(CodeElement instruction);

	/// Compare the two given instructions by index.
	int compare(CodeElement first, CodeElement second);

	/// Compare the two given points by index and offset.
	int compare(Point first, Point second);

	/// Check if the range defined by the start and end point contains the given point.
	/// This is exclusive on both ends, so equivalence does not count.
	///
	/// @throws IllegalArgumentException if the end comes before the start
	boolean rangeContains(Point from, Point to, Point point);
}
