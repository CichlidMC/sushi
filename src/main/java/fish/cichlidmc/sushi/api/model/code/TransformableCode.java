package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.api.model.HasAttachments;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.model.code.SafeCodeModelImpl;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transform.sliced.model.SlicedTransformableCode;
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

	/**
	 * A subset of {@link CodeElement}s that only contains {@link Instruction}s and {@link PseudoInstruction}s.
	 * @see SafeCodeModel#elements() the complete element list
	 */
	InstructionList instructions();

	/**
	 * @return a {@link Selection.Builder selection builder},
	 * which may be used to select regions of code for transformation
	 */
	Selection.Builder select();

	/**
	 * A stripped-down version of {@link CodeModel} that hides potentially dangerous methods.
	 */
	sealed interface SafeCodeModel permits SafeCodeModelImpl {
		int maxLocals();

		int maxStack();

		List<CodeElement> elements();

		List<ExceptionCatch> exceptionHandlers();

		AttributedElement attributed();
	}
}
