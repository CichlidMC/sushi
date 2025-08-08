package fish.cichlidmc.sushi.impl.apply;

import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public interface MethodGenerator {
	void generate(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> consumer);
}
