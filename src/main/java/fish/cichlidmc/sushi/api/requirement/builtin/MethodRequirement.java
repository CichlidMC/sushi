package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

/**
 * A requirement that indicates that a method must exist.
 * <p>
 * This requirement contextually depends on a {@link ClassRequirement}.
 */
public record MethodRequirement(String reason, String name, MethodTypeDesc desc, List<Requirement> chained) implements Requirement {
	public static final MapCodec<MethodRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), MethodRequirement::reason,
			Codec.STRING.fieldOf("name"), MethodRequirement::name,
			SushiCodecs.METHOD_DESC.fieldOf("desc"), MethodRequirement::desc,
			CHAINED_CODEC.fieldOf("chained"), MethodRequirement::chained,
			MethodRequirement::new
	);

	public MethodRequirement(String reason, String name, MethodTypeDesc desc, Requirement... chained) {
		this(reason, name, desc, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC;
	}
}
