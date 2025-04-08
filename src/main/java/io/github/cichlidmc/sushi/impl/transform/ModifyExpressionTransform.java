package io.github.cichlidmc.sushi.impl.transform;

import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.transform.TransformType;
import io.github.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import io.github.cichlidmc.sushi.api.util.MethodDescription;
import io.github.cichlidmc.sushi.api.util.MethodTarget;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import io.github.cichlidmc.tinycodecs.util.Either;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

public final class ModifyExpressionTransform implements Transform {
	public static final MapCodec<ModifyExpressionTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			MethodDescription.WITH_CLASS_CODEC.fieldOf("modifier"), transform -> transform.modifier,
			ModifyExpressionTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC);

	private final MethodTarget method;
	private final ExpressionTarget target;
	private final MethodDescription modifier;

	private ModifyExpressionTransform(MethodTarget method, ExpressionTarget target, MethodDescription modifier) {
		this.method = method;
		this.target = target;
		this.modifier = modifier;
	}

	@Override
	public boolean apply(ClassNode node) throws TransformException {
		Collection<MethodNode> methods = this.method.findOrThrow(node);
		Method modifier = this.getAndValidateModifier();

		boolean transformed = false;
		for (MethodNode method : methods) {
			Collection<AbstractInsnNode> targets = this.target.find(method.instructions);
			for (AbstractInsnNode target : targets) {
				method.instructions.insert(target, this.buildInjection(modifier));
				transformed = true;
			}
		}

		return transformed;
	}

	private InsnList buildInjection(Method modifier) {
		InsnList list = new InsnList();

		// TODO: insert parameters
		String desc = Type.getMethodDescriptor(modifier);
		String owner = Type.getInternalName(modifier.getDeclaringClass());
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, modifier.getName(), desc));

		return list;
	}

	private Method getAndValidateModifier() throws TransformException {
		Either<Method, MethodDescription.MethodMissingReason> maybeModifier = this.modifier.resolve();
		if (maybeModifier.isRight()) {
			throw new TransformException("Modifier method wasn't found - " + maybeModifier.right() + ": " + this.modifier);
		}

		Method modifier = maybeModifier.left();
		int modifiers = modifier.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw new TransformException("Modifier method must be public and static: " + this.modifier);
		}

		Class<?>[] params = modifier.getParameterTypes();
		if (params.length != 1) {
			throw new TransformException("Modifier method must take one parameter, the original value");
		}

		Class<?> returnType = modifier.getReturnType();

		if (returnType != params[0]) {
			throw new TransformException("Modifier method must take and return the same type: " + this.modifier);
		}

		return modifier;
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
