package fish.cichlidmc.sushi.api.condition.builtin;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.List;

/// A condition that is only met when all of its sub-conditions are met.
public record AllCondition(List<Condition> conditions) implements Condition {
	public static final MapCodec<AllCondition> CODEC = MapCodec.lazy(
			() -> Condition.CODEC.listOf()
					.xmap(AllCondition::new, AllCondition::conditions)
					.fieldOf("conditions")
	);

	@Override
	public boolean test(Context context) {
		for (Condition condition : this.conditions) {
			if (!condition.test(context)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public MapCodec<? extends Condition> type() {
		return CODEC;
	}
}
