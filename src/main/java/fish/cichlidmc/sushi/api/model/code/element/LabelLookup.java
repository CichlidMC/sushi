package fish.cichlidmc.sushi.api.model.code.element;

import fish.cichlidmc.sushi.impl.model.code.element.LabelLookupImpl;

import java.lang.classfile.Label;
import java.lang.classfile.instruction.LabelTarget;
import java.util.Optional;

public sealed interface LabelLookup permits LabelLookupImpl {
	/// Search for the [LabelTarget] corresponding to the given [Label].
	/// @return the [LabelTarget], if found
	Optional<InstructionHolder.Pseudo<LabelTarget>> find(Label label);

	/// @see #find(Label)
	InstructionHolder.Pseudo<LabelTarget> findOrThrow(Label label);
}
