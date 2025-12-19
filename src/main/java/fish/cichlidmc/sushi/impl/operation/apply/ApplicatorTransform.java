package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;

public record ApplicatorTransform(TransformableCodeImpl code, MethodGenerator methodGenerator, Operations.Validated operations) implements CodeTransform {
	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
	}

	@Override
	public void atStart(CodeBuilder builder) {
		new OperationApplicator(builder, this.code, this.methodGenerator, this.operations).run();
	}
}
