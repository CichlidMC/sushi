package fish.cichlidmc.sushi.api.model.code.element;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.impl.model.code.element.LocalVariablesImpl;
import org.jetbrains.annotations.Contract;

import java.lang.classfile.attribute.LocalVariableTableAttribute;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LocalVariable;
import java.util.Map;

/// A view of a method's Local Variable Table, normally exposed through [LocalVariableTableAttribute].
public sealed interface LocalVariables permits LocalVariablesImpl {
	/// Find all local variables valid in the scope of the given point.
	/// @return a new, mutable map of LVT indices to [entries][Entry]
	@Contract("_->new")
	Map<Integer, Entry> findInScope(Point point);

	sealed interface Entry permits LocalVariablesImpl.EntryImpl {
		InstructionHolder.Pseudo<LocalVariable> value();

		InstructionHolder.Pseudo<LabelTarget> start();

		InstructionHolder.Pseudo<LabelTarget> end();

		/// @return true if the given point lies between the start and end of this entry
		boolean contains(Point point);
	}
}
