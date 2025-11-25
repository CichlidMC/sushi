package fish.cichlidmc.sushi.api.condition.builtin;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

/**
 * A condition that inverts the result of its wrapped condition.
 */
public record NotCondition(Condition condition) implements Condition {
	public static final MapCodec<NotCondition> CODEC = MapCodec.lazy(
			() -> Condition.CODEC.xmap(NotCondition::new, NotCondition::condition).fieldOf("condition")
	);

	@Override
	public boolean test(Context context) {
		return !this.condition.test(context);
	}

	@Override
	public MapCodec<? extends Condition> type() {
		return CODEC;
	}
}
