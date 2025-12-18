package fish.cichlidmc.sushi.api.condition;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.impl.condition.ConditionContextImpl;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Set;

/// An arbitrary condition that must be met for a [ConfiguredTransformer] to be loaded.
public interface Condition {
	SimpleRegistry<MapCodec<? extends Condition>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<Condition> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), Condition::type);

	/// Check if this condition is met in the given context.
	boolean test(Context context);

	MapCodec<? extends Condition> type();

	/// Context available when checking conditions.
	sealed interface Context permits ConditionContextImpl {
		/// @return an immutable view of the set of registered transformers, both enabled and disabled
		Set<Id> transformers();
	}
}
