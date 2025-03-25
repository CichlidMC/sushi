package io.github.cichlidmc.sushi.impl.transform;

import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.point.InjectionPoint;
import io.github.cichlidmc.sushi.impl.util.MethodInClass;
import io.github.cichlidmc.sushi.impl.util.MethodTarget;
import io.github.cichlidmc.tinycodecs.MapCodec;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class InjectTransform implements Transform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			MethodInClass.CODEC.fieldOf("hook"), inject -> inject.hook,
			InjectTransform::new
	);

	private final MethodTarget method;
	private final InjectionPoint point;
	private final MethodInClass hook;

	private InjectTransform(MethodTarget method, InjectionPoint point, MethodInClass hook) {
		this.method = method;
		this.point = point;
		this.hook = hook;
	}

	@Override
	public boolean apply(ClassNode node) {
		AtomicBoolean didSomething = new AtomicBoolean(false);

		List<MethodNode> methods = this.method.filter(node.methods).collect(Collectors.toList());
		if (this.method.expect != -1 && methods.size() != this.method.expect) {
			throw new TransformException("Method target expected to match " + this.method.expect + " time(s), but matched " + methods.size() + " time(s).");
		}

		for (MethodNode method : methods) {
			Collection<AbstractInsnNode> targets = this.point.find(method.instructions);
			for (AbstractInsnNode target : targets) {
				method.instructions.insertBefore(target, this.buildInjection());
				didSomething.set(true);
			}
		}

		return didSomething.get();
	}

	private InsnList buildInjection() {
		InsnList list = new InsnList();
		// TODO: insert parameters
		String desc = Type.getMethodDescriptor(SushiInternals.CANCELLATION_TYPE);
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, this.hook.className, this.hook.name, desc));
		return list;
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return null;
	}
}
