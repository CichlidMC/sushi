package fish.cichlidmc.sushi.api.match.expression;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.MethodTypeDesc;
import java.util.Collection;

/// Defines an expression in a method body that can be selected for modification.
///
/// It is required that all selectable expressions result in the stack either shrinking or staying the same size.
public interface ExpressionSelector {
	SimpleRegistry<MapCodec<? extends ExpressionSelector>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<ExpressionSelector> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionSelector::codec);

	/// Find all expressions matching this selector.
	/// @throws TransformException if something goes wrong while selecting
	Collection<Found> find(TransformableCode code) throws TransformException;

	MapCodec<? extends ExpressionSelector> codec();

	/// An expression that has been found by a selector.
	/// @param selection a [Selection] surrounding the expression
	/// @param desc a descriptor defining the "inputs" and "output" of the found expression.
	///                The "inputs", or parameters, would be the types on the top of the stack which are consumed by the expression.
	///                The "output", or return type, would be the type left on the top of the stack by the expression.
	///                For example, for a static method invocation, this would just be its descriptor.
	record Found(Selection selection, MethodTypeDesc desc) {
	}
}
