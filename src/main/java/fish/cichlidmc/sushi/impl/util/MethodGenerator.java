package fish.cichlidmc.sushi.impl.util;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Set;
import java.util.function.Consumer;

@FunctionalInterface
public interface MethodGenerator {
	void generate(String name, MethodTypeDesc desc, Set<AccessFlag> flags, Consumer<MethodBuilder> consumer);

	static MethodGenerator of(ClassBuilder builder) {
		return (name, desc, flags, consumer) -> {
			int flagsMask = 0;

			for (AccessFlag flag : flags) {
				if (!flag.locations().contains(AccessFlag.Location.METHOD)) {
					throw new IllegalArgumentException("Flag not valid for methods: " + flag);
				}

				flagsMask |= flag.mask();
			}

			builder.withMethod(name, desc, flagsMask, consumer);
		};
	}
}
