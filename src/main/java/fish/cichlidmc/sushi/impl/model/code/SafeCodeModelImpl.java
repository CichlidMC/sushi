package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import org.glavo.classfile.AttributedElement;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.instruction.ExceptionCatch;

import java.util.List;

public final class SafeCodeModelImpl implements TransformableCode.SafeCodeModel {
	private final CodeModel model;
	private final List<CodeElement> elements;

	public SafeCodeModelImpl(CodeModel model, List<CodeElement> elements) {
		this.model = model;
		this.elements = elements;
	}

	@Override
	public int maxLocals() {
		return this.model.maxLocals();
	}

	@Override
	public int maxStack() {
		return this.model.maxStack();
	}

	@Override
	public List<CodeElement> elements() {
		return this.elements;
	}

	@Override
	public List<ExceptionCatch> exceptionHandlers() {
		return this.model.exceptionHandlers();
	}

	@Override
	public AttributedElement attributed() {
		return this.model;
	}

	public CodeModel unwrap() {
		return this.model;
	}
}
