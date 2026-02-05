package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.util.List;

/// A requirement that indicates that the class described by `desc` must exist.
///
/// This requirement is contextless.
public record ClassRequirement(String reason, ClassDesc desc, List<Requirement> chained) implements Requirement {
	public static final DualCodec<ClassRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), ClassRequirement::reason,
			ClassDescs.CLASS_OR_ARRAY_CODEC.fieldOf("class"), ClassRequirement::desc,
			CHAINED_CODEC.fieldOf("chained"), ClassRequirement::chained,
			ClassRequirement::new
	);

	public ClassRequirement(String reason, ClassDesc desc, Requirement... chained) {
		this(reason, desc, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC.mapCodec();
	}
}
