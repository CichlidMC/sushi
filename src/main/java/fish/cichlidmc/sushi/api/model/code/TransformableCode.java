package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.HasAttachments;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transformer.slice.SlicedTransformableCode;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.util.NavigableSet;

public sealed interface TransformableCode extends HasAttachments permits TransformableCodeImpl, SlicedTransformableCode {
	CodeModel model();

	TransformableMethod owner();

	/// A subset of [CodeElement]s that only contains [Instruction]s and [PseudoInstruction]s.
	///
	/// Each instruction is wrapped in an [InstructionHolder], which provides location information.
	///
	/// All [CodeElement]s may be found via [the model][#model()].
	NavigableSet<InstructionHolder<?>> instructions();

	/// @return a [selection builder][Selection.Builder],
	/// which may be used to select regions of code for transformation
	Selection.Builder select();
}
