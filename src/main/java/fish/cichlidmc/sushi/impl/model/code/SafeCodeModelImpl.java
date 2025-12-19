package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;

import java.lang.classfile.AttributedElement;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.List;

public final class SafeCodeModelImpl implements TransformableCode.SafeCodeModel {
	private final CodeModel model;
	private final List<CodeElement> elements;

	public SafeCodeModelImpl(CodeModel model, List<CodeElement> elements) {
		this.model = model;
		this.elements = elements;
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
}
