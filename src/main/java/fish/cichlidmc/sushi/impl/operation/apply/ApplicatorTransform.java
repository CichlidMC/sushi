package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.apply.MethodGenerator;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;

public record ApplicatorTransform(TransformableCodeImpl code, MethodGenerator methodGenerator, Operations.Validated operations) implements CodeTransform {
	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
	}

	@Override
	public void atStart(CodeBuilder builder) {
		new OperationApplicator(builder, this.code, this.methodGenerator, this.operations).run();
	}
}
