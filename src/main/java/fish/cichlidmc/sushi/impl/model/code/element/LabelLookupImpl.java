package fish.cichlidmc.sushi.impl.model.code.element;

import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.LabelLookup;
import fish.cichlidmc.sushi.api.transformer.TransformException;

import java.lang.classfile.Label;
import java.lang.classfile.instruction.LabelTarget;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;

public record LabelLookupImpl(Map<Label, InstructionHolder.Pseudo<LabelTarget>> map) implements LabelLookup {
	@Override
	public Optional<InstructionHolder.Pseudo<LabelTarget>> find(Label label) {
		return Optional.ofNullable(this.map.get(label));
	}

	@Override
	public InstructionHolder.Pseudo<LabelTarget> findOrThrow(Label label) {
		return this.find(label).orElseThrow(
				() -> new TransformException("Missing LabelTarget for label: " + label)
		);
	}

	public static LabelLookup create(NavigableSet<InstructionHolder<?>> instructions) {
		Map<Label, InstructionHolder.Pseudo<LabelTarget>> map = new HashMap<>();

		for (InstructionHolder<?> instruction : instructions) {
			if (instruction.get() instanceof LabelTarget target) {
				map.put(target.label(), instruction.checkHoldingPseudo(LabelTarget.class));
			}
		}

		return new LabelLookupImpl(Collections.unmodifiableMap(map));
	}
}
