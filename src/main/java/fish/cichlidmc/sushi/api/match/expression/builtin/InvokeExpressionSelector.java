package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

/// An [ExpressionSelector] matching method invocations.
public record InvokeExpressionSelector(MethodSelector selector) implements ExpressionSelector {
	public static final MapCodec<InvokeExpressionSelector> CODEC = MethodSelector.CODEC.xmap(
			InvokeExpressionSelector::new, InvokeExpressionSelector::selector
	).fieldOf("method");

	@Override
	public List<Found> find(TransformableCode code) throws TransformException {
		return this.selector.find(code.instructions()).stream().map(instruction -> {
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
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC;
	}
}
