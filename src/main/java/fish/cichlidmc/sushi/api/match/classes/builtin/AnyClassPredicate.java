package fish.cichlidmc.sushi.api.match.classes.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/// A compound [ClassPredicate] which matches all classes matched by any of its components.
public record AnyClassPredicate(List<ClassPredicate> entries) implements ClassPredicate {
	// this needs to be lazy because of the circular reference between it and ClassPredicate.CODEC
	public static final Codec<AnyClassPredicate> CODEC = Codec.lazy(
			() -> ClassPredicate.CODEC.listOf().xmap(AnyClassPredicate::new, AnyClassPredicate::entries)
	);
	public static final MapCodec<AnyClassPredicate> MAP_CODEC = CODEC.fieldOf("entries");

	public AnyClassPredicate(ClassPredicate... entries) {
		this(List.of(entries));
	}

	@Override
	public boolean test(Context context) {
		for (ClassPredicate entry : this.entries) {
			if (entry.test(context)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Optional<Set<ClassDesc>> concreteMatches() {
		Set<ClassDesc> matches = new HashSet<>();

		for (ClassPredicate entry : this.entries) {
			Optional<Set<ClassDesc>> concrete = entry.concreteMatches();
			if (concrete.isPresent()) {
				matches.addAll(concrete.get());
			} else {
				// if any entry has non-concrete targets, then the slow path is necessary
				return Optional.empty();
			}
		}

		return Optional.of(matches);
	}

	@Override
	public MapCodec<? extends ClassPredicate> codec() {
		return MAP_CODEC;
	}
}
