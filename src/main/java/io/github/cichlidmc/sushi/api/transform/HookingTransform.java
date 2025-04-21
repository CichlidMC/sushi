package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.util.method.MethodDescription;
import io.github.cichlidmc.sushi.api.util.method.MethodTarget;
import io.github.cichlidmc.tinycodecs.util.Either;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class HookingTransform extends MethodTargetingTransform {
	protected final MethodDescription hook;

	protected HookingTransform(MethodTarget method, MethodDescription hook) {
		super(method);
		this.hook = hook;
	}

	@Override
	protected final boolean apply(TransformContext context, MethodNode method) throws TransformException {
		Method hook = this.getAndValidateHook();
		return this.apply(context, method, hook);
	}

	protected abstract boolean apply(TransformContext context, MethodNode method, Method hook) throws TransformException;

	protected Method getAndValidateHook() throws TransformException {
		Either<Method, MethodDescription.MethodMissingReason> maybeHook = this.hook.resolve();
		if (maybeHook.isRight()) {
			throw new TransformException("Hook method wasn't found - " + maybeHook.right() + ": " + this.hook);
		}

		Method hook = maybeHook.left();
		int modifiers = hook.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw new TransformException("Hook method must be public and static: " + this.hook);
		}

		return hook;
	}
}
