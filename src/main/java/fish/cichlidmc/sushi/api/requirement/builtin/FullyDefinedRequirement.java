package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.List;

/**
 * Requirement indicating that a class or interface must be fully defined.
 * This means that a subclass should not need to implement any abstract methods.
 * <p>
 * This requirement contextually depends on a {@link ClassRequirement}.
 */
public record FullyDefinedRequirement(String reason, List<Requirement> chained) implements Requirement {
	public static final MapCodec<FullyDefinedRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), FullyDefinedRequirement::reason,
			CHAINED_CODEC.fieldOf("chained"), FullyDefinedRequirement::chained,
			FullyDefinedRequirement::new
	);

	public FullyDefinedRequirement(String reason, Requirement... chained) {
		this(reason, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC;
	}
}
