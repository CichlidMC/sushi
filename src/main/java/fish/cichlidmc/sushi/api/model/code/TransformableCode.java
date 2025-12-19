package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.HasAttachments;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.model.code.SafeCodeModelImpl;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transformer.SlicedTransformableCode;

import java.lang.classfile.AttributedElement;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.List;

public sealed interface TransformableCode extends HasAttachments permits TransformableCodeImpl, SlicedTransformableCode {
	SafeCodeModel model();

	TransformableMethod owner();

	/// A subset of [CodeElement]s that only contains [Instruction]s and [PseudoInstruction]s.
	/// @see SafeCodeModel#elements() the complete element list
	InstructionList instructions();

	/// @return a [selection builder][Selection.Builder],
	/// which may be used to select regions of code for transformation
	Selection.Builder select();

	/// A stripped-down version of [CodeModel] that hides potentially dangerous methods.
	sealed interface SafeCodeModel permits SafeCodeModelImpl {
		List<CodeElement> elements();

		List<ExceptionCatch> exceptionHandlers();

		AttributedElement attributed();
	}
}
