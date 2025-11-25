package fish.cichlidmc.sushi.api.condition.builtin;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

/**
 * A condition that is met if a transformer with the given ID is present.
 * <p>
 * The definition of "present" is important. Sushi setup begins by collecting all
 * possibly enabled transformers. Sushi then removes any whose conditions are not met.
 * The *initial* set is the set of "present" transformers, not the final one, since
 * that would create a cyclical dependency.
 */
public record TransformerPresentCondition(Id id) implements Condition {
	public static final MapCodec<TransformerPresentCondition> CODEC = Id.CODEC.xmap(
			TransformerPresentCondition::new, TransformerPresentCondition::id
	).fieldOf("id");

	@Override
	public boolean test(Context context) {
		return context.transformers().contains(this.id);
	}

	@Override
	public MapCodec<? extends Condition> type() {
		return CODEC;
	}
}
