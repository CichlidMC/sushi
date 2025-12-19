package fish.cichlidmc.sushi.impl.operation.apply;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.util.function.Consumer;

public final class CodeElementCollector implements CodeTransform {
	private final Consumer<CodeElement> output;

	public CodeElementCollector(Consumer<CodeElement> output) {
		this.output = output;
	}

	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
		this.output.accept(element);
	}
}
