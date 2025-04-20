package io.github.cichlidmc.sushi.impl.transform.expression;

import io.github.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import io.github.cichlidmc.sushi.api.transform.expression.FoundExpressionTargets;
import io.github.cichlidmc.sushi.api.util.JavaType;
import io.github.cichlidmc.sushi.api.util.method.MethodTarget;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collection;

public final class InvokeExpressionTarget implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodTarget.CODEC.xmap(
			InvokeExpressionTarget::new, invoke -> invoke.target
	).fieldOf("method");

	private final MethodTarget target;

	private InvokeExpressionTarget(MethodTarget target) {
		this.target = target;
	}

	@Override
	@Nullable
	public FoundExpressionTargets find(InsnList instructions) {
		Collection<MethodInsnNode> found = this.target.findOrThrow(instructions, true);
		if (found.isEmpty())
			return null;

		MethodInsnNode any = found.iterator().next();
		// non-static methods have an implied reference on top of the stack
		boolean addRef = any.getOpcode() != Opcodes.INVOKESTATIC;

		Type[] asmParams = Type.getArgumentTypes(any.desc);
		int offset = addRef ? 1 : 0;
		JavaType[] params = new JavaType[asmParams.length + offset];

		if (addRef) {
			params[0] = JavaType.of(Type.getObjectType(any.owner));
		}

		for (int i = offset; i < params.length; i++) {
			int asmIndex = i - offset;
			params[i] = JavaType.of(asmParams[asmIndex]);
		}

		JavaType returnType = JavaType.of(Type.getReturnType(any.desc));

		return new FoundExpressionTargets(found, params, returnType);
	}

	@Override
	public String describe() {
		return "all invokes of " + this.target.description;
	}

	@Override
	public MapCodec<? extends ExpressionTarget> codec() {
		return CODEC;
	}
}
