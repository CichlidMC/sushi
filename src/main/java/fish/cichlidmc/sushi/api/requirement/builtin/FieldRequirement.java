package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.util.List;

/**
 * A requirement that indicates that the field identified by {@link #name} and {@link #type} exists.
 * <p>
 * This requirement contextually depends on a {@link ClassRequirement}.
 */
public record FieldRequirement(String reason, String name, ClassDesc type, List<Requirement> chained) implements Requirement {
	public static final MapCodec<FieldRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), FieldRequirement::reason,
			Codec.STRING.fieldOf("name"), FieldRequirement::name,
			ClassDescs.ANY_CODEC.fieldOf("type"), FieldRequirement::type,
			CHAINED_CODEC.fieldOf("chained"), FieldRequirement::chained,
			FieldRequirement::new
	);

	public FieldRequirement(String reason, String name, ClassDesc type, Requirement... chained) {
		this(reason, name, type, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC;
	}
}
