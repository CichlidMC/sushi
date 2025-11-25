package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.List;

/**
 * Requirement indicating that a class or interface must be fully defined.
 * This means that a subclass should not need to implement any abstract methods.
 * <p>
 * This requirement contextually depends on a {@link ClassRequirement}.
 */
public record FullyDefinedRequirement(String reason, List<Requirement> chained) implements Requirement {
	public static final DualCodec<FullyDefinedRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), FullyDefinedRequirement::reason,
			CHAINED_CODEC.fieldOf("chained"), FullyDefinedRequirement::chained,
			FullyDefinedRequirement::new
	);

	public FullyDefinedRequirement(String reason, Requirement... chained) {
		this(reason, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC.mapCodec();
	}
}
