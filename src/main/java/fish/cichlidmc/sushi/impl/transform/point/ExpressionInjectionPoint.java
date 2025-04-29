package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.expression.FoundExpressionTargets;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;

public class ExpressionInjectionPoint implements InjectionPoint {
	public static final MapCodec<ExpressionInjectionPoint> CODEC = CompositeCodec.of(
			ExpressionTarget.CODEC.fieldOf("target"), point -> point.target,
			Shift.CODEC.optional(Shift.BEFORE).fieldOf("shift"), point -> point.shift,
			ExpressionInjectionPoint::new
	);

	private final ExpressionTarget target;
	private final Shift shift;

	public ExpressionInjectionPoint(ExpressionTarget target, Shift shift) {
		this.target = target;
		this.shift = shift;
	}

	@Override
	@Nullable
	public Collection<? extends AbstractInsnNode> find(InsnList instructions) {
		FoundExpressionTargets targets = this.target.find(instructions);
		if (targets == null)
			return null;
		return targets.instructions;
	}

	@Override
	public Shift shift() {
		return this.shift;
	}

	@Override
	public String describe() {
		return this.shift.name + " " + this.target.describe();
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}
}
