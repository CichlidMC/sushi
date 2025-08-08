package fish.cichlidmc.sushi.impl.operation.apply;

import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;

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
