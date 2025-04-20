package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.util.method.MethodTarget;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public abstract class MethodTargetingTransform implements Transform {
	protected final MethodTarget method;

	protected MethodTargetingTransform(MethodTarget method) {
		this.method = method;
	}

	@Override
	public final boolean apply(ClassNode node) throws TransformException {
		Collection<MethodNode> methods = this.method.findOrThrow(node);

		boolean transformed = false;

		for (MethodNode method : methods) {
			transformed |= this.apply(node, method);
		}

		return transformed;
	}

	protected abstract boolean apply(ClassNode clazz, MethodNode method) throws TransformException;
}
