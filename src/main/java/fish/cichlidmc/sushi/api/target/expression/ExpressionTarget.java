package fish.cichlidmc.sushi.api.target.expression;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.MethodTypeDesc;
import java.util.Collection;

/// Defines an expression in a method body that can be targeted for modification.
///
/// It is required that all targetable expressions result in the stack either shrinking or staying the same size.
public interface ExpressionTarget {
	SimpleRegistry<MapCodec<? extends ExpressionTarget>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<ExpressionTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionTarget::codec);

	/// Find all expressions matching this target.
	/// @throws TransformException if something goes wrong while finding targets
	Collection<Found> find(TransformableCode code) throws TransformException;

	MapCodec<? extends ExpressionTarget> codec();

	/// A selection that has been found that matches the targeted expression.
	/// @param desc a descriptor defining the "inputs" and "output" of the found expression.
	///                The "inputs", or parameters, would be the types on the top of the stack which are consumed by the expression.
	///                The "output", or return type, would be the type left on the top of the stack by the expression.
	///                For example, for a static method invocation, this would just be its descriptor.
	record Found(Selection selection, MethodTypeDesc desc) {
	}
}
