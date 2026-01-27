package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.DirectlyTransformable;
import fish.cichlidmc.sushi.api.model.HasAttachments;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.LabelLookup;
import fish.cichlidmc.sushi.api.model.code.element.LocalVariables;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transformer.slice.SlicedTransformableCode;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.PseudoInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.util.NavigableSet;
import java.util.Optional;

public sealed interface TransformableCode extends HasAttachments, DirectlyTransformable<DirectTransform.Code> permits TransformableCodeImpl, SlicedTransformableCode {
	CodeModel model();

	TransformableMethod owner();

	/// A subset of [CodeElement]s that only contains [Instruction]s and [PseudoInstruction]s.
	///
	/// Each instruction is wrapped in an [InstructionHolder], which provides location information.
	///
	/// All [CodeElement]s may be found via [the model][#model()].
	NavigableSet<InstructionHolder<?>> instructions();

	/// @return a [LabelLookup] allowing for finding the [LabelTarget]s of [Label]s
	LabelLookup labels();

	/// @return a view of the Local Variable Table, if present
	Optional<LocalVariables> locals();

	/// @return a [selection builder][Selection.Builder],
	/// which may be used to select regions of code for transformation
	Selection.Builder select();
}
