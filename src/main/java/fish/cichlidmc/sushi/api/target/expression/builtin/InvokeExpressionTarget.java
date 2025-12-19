package fish.cichlidmc.sushi.api.target.expression.builtin;

import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

/// An [ExpressionTarget] matching method invocations.
public record InvokeExpressionTarget(MethodTarget target) implements ExpressionTarget {
	public static final MapCodec<InvokeExpressionTarget> CODEC = MethodTarget.CODEC.xmap(
			InvokeExpressionTarget::new, InvokeExpressionTarget::target
	).fieldOf("method");

	@Override
	public List<Found> find(TransformableCode code) throws TransformException {
		return this.target.find(code.instructions()).stream().map(instruction -> {
			InvokeInstruction invoke = instruction.get();
			MethodTypeDesc desc = invoke.typeSymbol();
			if (invoke.opcode() != Opcode.INVOKESTATIC) {
				// non-static methods have an implicit reference to the receiver on the stack
				desc = desc.insertParameterTypes(0, invoke.owner().asSymbol());
			}
			Selection selection = code.select().only(instruction);
			return new Found(selection, desc);
		}).toList();
	}

	@Override
	public MapCodec<? extends ExpressionTarget> codec() {
		return CODEC;
	}
}
