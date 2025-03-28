package io.github.cichlidmc.sushi.impl.transform;

import io.github.cichlidmc.sushi.api.transform.Cancellation;
import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.impl.point.InjectionPoint;
import io.github.cichlidmc.sushi.impl.util.MethodDescription;
import io.github.cichlidmc.sushi.impl.util.MethodTarget;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import io.github.cichlidmc.tinycodecs.util.Either;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

public final class InjectTransform implements Transform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			MethodDescription.WITH_CLASS_CODEC.fieldOf("hook"), inject -> inject.hook,
			InjectTransform::new
	);

	private final MethodTarget method;
	private final InjectionPoint point;
	private final MethodDescription hook;

	private InjectTransform(MethodTarget method, InjectionPoint point, MethodDescription hook) {
		this.method = method;
		this.point = point;
		this.hook = hook;
	}

	@Override
	public boolean apply(ClassNode node) {
		Collection<MethodNode> methods = this.method.findOrThrow(node);
		Method hook = this.getAndValidateHook();

		boolean transformed = false;
		for (MethodNode method : methods) {
			Collection<AbstractInsnNode> targets = this.point.find(method.instructions);
			for (AbstractInsnNode target : targets) {
				method.instructions.insertBefore(target, this.buildInjection(hook));
				transformed = true;
			}
		}

		return transformed;
	}

	private InsnList buildInjection(Method hook) {
		InsnList list = new InsnList();

		// TODO: insert parameters
		String desc = Type.getMethodDescriptor(hook);
		String owner = Type.getInternalName(hook.getDeclaringClass());
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, hook.getName(), desc));

		if (hook.getReturnType() != Void.TYPE) {
			// TODO: use the cancellation
			list.add(new InsnNode(Opcodes.POP));
		}

		return list;
	}

	private Method getAndValidateHook() throws TransformException {
		Either<Method, MethodDescription.MethodMissingReason> maybeHook = this.hook.resolve();
		if (maybeHook.isRight()) {
			throw new TransformException("Hook for inject wasn't found - " + maybeHook.right() + ": " + this.hook);
		}

		Method hook = maybeHook.left();
		int modifiers = hook.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw new TransformException("Hook method must be public and static: " + this.hook);
		}

		Class<?> returnType = hook.getReturnType();
		if (returnType != Void.TYPE && returnType != Cancellation.class) {
			throw new TransformException("Hook method must either return void or Cancellation: " + this.hook);
		}

		return hook;
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}
}
