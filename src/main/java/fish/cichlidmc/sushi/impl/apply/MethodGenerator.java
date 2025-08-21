package fish.cichlidmc.sushi.impl.apply;

import org.glavo.classfile.AccessFlags;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public interface MethodGenerator {
	void generate(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> consumer);

	static MethodGenerator of(ClassBuilder builder) {
		// the extracted classfile API discards the flags provided here when builder is a ChainedClassBuilder
		// FIXME: this workaround will no longer be necessary in java 24+
		return (name, desc, flags, consumer) -> builder.withMethod(name, desc, flags, method -> {
			method.with(AccessFlags.ofMethod(flags));
			consumer.accept(method);
		});
	}
}
