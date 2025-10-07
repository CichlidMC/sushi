package fish.cichlidmc.sushi.api.condition.builtin;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.tinycodecs.map.MapCodec;

/**
 * A condition that is met if a transformer with the given ID is present.
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
