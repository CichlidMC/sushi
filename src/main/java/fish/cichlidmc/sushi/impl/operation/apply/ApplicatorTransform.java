package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;

public record ApplicatorTransform(TransformableCodeImpl code, ClassBuilder classBuilder, Operations.Validated operations) implements CodeTransform {
	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
	}

	@Override
	public void atStart(CodeBuilder builder) {
		new OperationApplicator(builder, this.code, this.classBuilder, this.operations).run();
	}
}
