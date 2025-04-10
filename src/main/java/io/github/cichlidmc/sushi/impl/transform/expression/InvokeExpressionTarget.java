package io.github.cichlidmc.sushi.impl.transform.expression;

import io.github.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import io.github.cichlidmc.sushi.api.transform.expression.FoundExpressionTarget;
import io.github.cichlidmc.sushi.api.util.JavaType;
import io.github.cichlidmc.sushi.api.util.MethodTarget;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.stream.Collectors;

public final class InvokeExpressionTarget implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodTarget.CODEC.xmap(
			InvokeExpressionTarget::new, invoke -> invoke.target
	).fieldOf("method");

	private final MethodTarget target;

	private InvokeExpressionTarget(MethodTarget target) {
		this.target = target;
	}

	@Override
	public Collection<FoundExpressionTarget> find(InsnList instructions) {
		return this.target.findOrThrow(instructions).stream().map(node -> {
			Type returnType = Type.getReturnType(node.desc);
			return new FoundExpressionTarget(node, JavaType.of(returnType));
		}).collect(Collectors.toList());
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
