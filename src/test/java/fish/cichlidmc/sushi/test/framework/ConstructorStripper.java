package fish.cichlidmc.sushi.test.framework;

import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.MethodModel;

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
