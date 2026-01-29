package fish.cichlidmc.sushi.api.match.expression;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// Defines an expression in a method body that can be selected for modification.
public interface ExpressionSelector {
	SimpleRegistry<MapCodec<? extends ExpressionSelector>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<ExpressionSelector> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionSelector::codec);

	/// Find all expressions matching this selector.
	/// @throws TransformException if something goes wrong while selecting
	Collection<Found> find(TransformableCode code) throws TransformException;

	MapCodec<? extends ExpressionSelector> codec();

	/// An expression that has been found by a selector.
	/// @param selection a [Selection] surrounding the expression
	/// @param delta a [StackDelta] describing the changes made to the top of the stack by the selected code
	record Found(Selection selection, StackDelta delta) {
	}
}
