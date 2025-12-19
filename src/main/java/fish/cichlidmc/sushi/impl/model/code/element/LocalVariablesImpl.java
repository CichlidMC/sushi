package fish.cichlidmc.sushi.impl.model.code.element;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.LabelLookup;
import fish.cichlidmc.sushi.api.model.code.element.LocalVariables;

import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LocalVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

public record LocalVariablesImpl(List<Entry> entries) implements LocalVariables {
	@Override
	public Map<Integer, Entry> findInScope(Point point) {
		Map<Integer, Entry> map = new HashMap<>();

		for (Entry entry : this.entries) {
			if (point.compareTo(entry.start()) > 0 && point.compareTo(entry.end()) < 0) {
				int slot = entry.value().get().slot();

				if (map.containsKey(slot)) {
					throw new IllegalStateException("Multiple locals with the same slot are in scope");
				}

				map.put(slot, entry);
			}
		}

		return map;
	}

	public static LocalVariables create(NavigableSet<InstructionHolder<?>> instructions, LabelLookup labels) {
		List<Entry> entries = new ArrayList<>();

		for (InstructionHolder<?> instruction : instructions) {
			if (instruction.get() instanceof LocalVariable local) {
				InstructionHolder.Pseudo<LabelTarget> start = labels.findOrThrow(local.startScope());
				InstructionHolder.Pseudo<LabelTarget> end = labels.findOrThrow(local.endScope());
				entries.add(new EntryImpl(instruction.checkHoldingPseudo(LocalVariable.class), start, end));
			}
		}

		return new LocalVariablesImpl(entries);
	}

	public record EntryImpl(
			InstructionHolder.Pseudo<LocalVariable> value,
			InstructionHolder.Pseudo<LabelTarget> start,
			InstructionHolder.Pseudo<LabelTarget> end
	) implements LocalVariables.Entry {}
}
