package fish.cichlidmc.sushi.impl.transform.expression;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record InvokeExpressionTarget(MethodTarget target) implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodTarget.CODEC.xmap(
			InvokeExpressionTarget::new, InvokeExpressionTarget::target
	).fieldOf("method");

	@Nullable
	@Override
	public Found find(TransformableCode code) throws TransformException {
		List<InvokeInstruction> found = this.target.findOrThrow(code.instructions(), true);
		if (found.isEmpty())
			return null;

		InvokeInstruction any = found.getFirst();
		MethodTypeDesc desc = any.typeSymbol();

		if (any.opcode() != Opcode.INVOKESTATIC) {
			// non-static methods have an implied reference on top of the stack
			desc = desc.insertParameterTypes(0, any.owner().asSymbol());
		}

		return new Found(found.stream().map(instruction -> code.select().only(instruction)).toList(), desc);
	}

	@Override
	public MapCodec<? extends ExpressionTarget> codec() {
		return CODEC;
	}
}
