package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.util.Instructions;
import org.glavo.classfile.CodeElement;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class InstructionListImpl implements InstructionList {
	private final List<CodeElement> list;
	private final Map<CodeElement, Integer> indices;

	private InstructionListImpl(List<CodeElement> instructions) {
		this.list = Collections.unmodifiableList(instructions);
		this.indices = new IdentityHashMap<>();
		for (int i = 0; i < this.list.size(); i++) {
			CodeElement instruction = this.list.get(i);
			if (this.indices.put(instruction, i) != null) {
				throw new IllegalArgumentException("Duplicate instruction: " + instruction);
			}
		}
	}

	@Override
	public List<CodeElement> asList() {
		return this.list;
	}

	@Override
	public int indexOf(CodeElement instruction) {
		Integer i = this.indices.get(instruction);
		if (i != null)
			return i;

		throw new IllegalArgumentException("Instruction is not in this list: " + instruction);
	}

	@Override
	public int compare(CodeElement first, CodeElement second) {
		return Integer.compare(this.indexOf(first), this.indexOf(second));
	}

	@Override
	public int compare(Point first, Point second) {
		int byIndex = this.compare(first.instruction(), second.instruction());
		if (byIndex != 0) {
			return byIndex;
		}

		return first.offset().compareTo(second.offset());
	}

	@Override
	public InstructionList subList(Point from, Point to) {
		if (this.compare(from, to) > 0) {
			throw new IllegalArgumentException("Sub-list end comes before start");
		}

		int fromIndex = this.indexForSubList(from);
		int toIndex = this.indexForSubList(to);

		List<CodeElement> subList = this.asList().subList(fromIndex, toIndex);
		return new InstructionListImpl(subList);
	}

	private int indexForSubList(Point point) {
		int i = this.indexOf(point.instruction());
		return point.offset() == Point.Offset.AFTER ? i + 1 : i;
	}

	public static InstructionListImpl ofElements(List<CodeElement> elements) {
		List<CodeElement> instructions = elements.stream().filter(Instructions::isInstruction).toList();
		return new InstructionListImpl(instructions);
	}
}
