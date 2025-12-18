package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.HasAttachments;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.model.code.SafeCodeModelImpl;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transformer.SlicedTransformableCode;
import org.glavo.classfile.AttributedElement;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.PseudoInstruction;
import org.glavo.classfile.instruction.ExceptionCatch;

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
		int maxLocals();

		int maxStack();

		List<CodeElement> elements();

		List<ExceptionCatch> exceptionHandlers();

		AttributedElement attributed();
	}
}
