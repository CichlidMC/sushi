package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.util.List;

/// A requirement that indicates that a class or interface must extend or implement
/// `parent`, and therefore also indicates that `parent` is a class that exists.
///
/// This requirement contextually depends on a [ClassRequirement].
public record InheritanceRequirement(String reason, ClassDesc parent, List<Requirement> chained) implements Requirement {
	public static final DualCodec<InheritanceRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), InheritanceRequirement::reason,
			ClassDescs.CLASS_OR_ARRAY_CODEC.fieldOf("parent"), InheritanceRequirement::parent,
			CHAINED_CODEC.fieldOf("chained"), InheritanceRequirement::chained,
			InheritanceRequirement::new
	);

	public InheritanceRequirement(String reason, ClassDesc parent, Requirement... chained) {
		this(reason, parent, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC.mapCodec();
	}
}
