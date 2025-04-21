package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.util.method.MethodTarget;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public abstract class MethodTargetingTransform implements Transform {
	protected final MethodTarget method;

	protected MethodTargetingTransform(MethodTarget method) {
		this.method = method;
	}

	@Override
	public final boolean apply(TransformContext context) throws TransformException {
		Collection<MethodNode> methods = this.method.findOrThrow(context.node());

		boolean transformed = false;

		for (MethodNode method : methods) {
			transformed |= this.apply(context, method);
		}

		return transformed;
	}

	protected abstract boolean apply(TransformContext context, MethodNode method) throws TransformException;
}
