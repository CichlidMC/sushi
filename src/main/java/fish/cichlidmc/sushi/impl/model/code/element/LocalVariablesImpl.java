package fish.cichlidmc.sushi.impl.model.code.element;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.LabelLookup;
import fish.cichlidmc.sushi.api.model.code.element.LocalVariables;

import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.reflect.AccessFlag;
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
			if (entry.contains(point)) {
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
	) implements Entry {
		@Override
		public boolean contains(Point point) {
			return this.isAfterStart(point) && this.isBeforeEnd(point);
		}

		private boolean isAfterStart(Point point) {
			if (point.compareTo(this.start) > 0)
				return true;

			// special case: this local is a parameter, which *should* be in scope for the whole method.
			// we do this to handle weirdness with insertions at the head of the method.
			// parameters are technically not in scope until the first label, but an insertion at the head
			// will come before that. we can't move it later, since then it might get caught in loop or try
			// statements. this isn't really sound for arbitrary bytecode, but if the LVT is present it should
			// be pretty normal anyway.
			return this.isParameter();
		}

		private boolean isBeforeEnd(Point point) {
			return point.compareTo(this.end) < 0;
		}

		private boolean isParameter() {
			int slot = this.value.get().slot();
			TransformableMethod method = this.value.owner().owner();
			// parameters, +1 if non-static
			int implicitSlots = method.key().desc().parameterCount() + (
					method.model().flags().flags().contains(AccessFlag.STATIC) ? 0 : 1
			);

			return slot < implicitSlots;
		}
	}
}
