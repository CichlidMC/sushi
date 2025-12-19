package fish.cichlidmc.sushi.test.framework;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.MethodModel;

public enum ConstructorStripper implements ClassTransform {
	INSTANCE;

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
		if (!isConstructor(element)) {
			builder.with(element);
		}
	}

	private static boolean isConstructor(ClassElement element) {
		return element instanceof MethodModel method && method.methodName().equalsString("<init>");
	}
}
