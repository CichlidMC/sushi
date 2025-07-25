package fish.cichlidmc.sushi.impl.util;

import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;

public enum NoOpClassTransform implements ClassTransform {
	INSTANCE;

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
		builder.with(element);
	}

	@Override
	public ClassTransform andThen(ClassTransform transform) {
		return transform;
	}
}
