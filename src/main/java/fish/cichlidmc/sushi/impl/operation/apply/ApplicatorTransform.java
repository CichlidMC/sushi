package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.apply.MethodGenerator;
import fish.cichlidmc.sushi.impl.operation.Operations;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;

import java.util.List;

public final class ApplicatorTransform implements CodeTransform {
	private final List<CodeElement> elements;
	private final MethodGenerator methodGenerator;
	private final Operations.Validated operations;

	public ApplicatorTransform(List<CodeElement> elements, MethodGenerator methodGenerator, Operations.Validated operations) {
		this.elements = elements;
		this.methodGenerator = methodGenerator;
		this.operations = operations;
	}

	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
	}

	@Override
	public void atStart(CodeBuilder builder) {
		new OperationApplicator(builder, this.elements, this.methodGenerator, this.operations).run();
	}
}
