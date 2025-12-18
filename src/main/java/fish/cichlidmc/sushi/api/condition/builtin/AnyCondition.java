package fish.cichlidmc.sushi.api.condition.builtin;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.List;

/// A condition that is met when any of its sub-conditions are met.
public record AnyCondition(List<Condition> conditions) implements Condition {
	public static final MapCodec<AnyCondition> CODEC = MapCodec.lazy(
			() -> Condition.CODEC.listOf()
					.xmap(AnyCondition::new, AnyCondition::conditions)
					.fieldOf("conditions")
	);

	@Override
	public boolean test(Context context) {
		for (Condition condition : this.conditions) {
			if (condition.test(context)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public MapCodec<? extends Condition> type() {
		return CODEC;
	}
}
