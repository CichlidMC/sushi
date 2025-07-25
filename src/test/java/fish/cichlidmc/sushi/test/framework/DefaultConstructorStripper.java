package fish.cichlidmc.sushi.test.framework;

import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.MethodModel;

import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

public enum DefaultConstructorStripper implements ClassTransform {
	INSTANCE;

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
		if (!isDefaultConstructor(element)) {
			builder.with(element);
		}
	}

	private static boolean isDefaultConstructor(ClassElement element) {
		if (!(element instanceof MethodModel method) || !method.methodName().stringValue().equals("<init>"))
			return false;

		MethodTypeDesc desc = method.methodTypeSymbol();
		if (desc.parameterCount() != 0 || !desc.returnType().equals(ConstantDescs.CD_void))
			return false;

		// technically not correct because code isn't checked, but good enough for these tests.
		return true;
	}
}
