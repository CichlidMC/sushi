package fish.cichlidmc.sushi.api.requirement.builtin;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.impl.requirement.FlagsRequirementBuilderImpl;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.AccessFlag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines two sets of access flags: those that are required, and those that are forbidden.
 * Can be matched against a set of flags. Matches when all required flags are present and no forbidden ones are.
 * <p>
 * This requirement is contextual, and must be chained after any of the following:
 * <ul>
 *     <li>{@link ClassRequirement}</li>
 *     <li>{@link FieldRequirement}</li>
 *     <li>{@link MethodRequirement}</li>
 * </ul>
 */
public record FlagsRequirement(String reason, Set<Entry> flags, List<Requirement> chained) implements Requirement {
	private static final Codec<Set<Entry>> entrySetCodec = SushiCodecs.setOf(Entry.CODEC, HashSet::new);

	public static final MapCodec<FlagsRequirement> CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("reason"), FlagsRequirement::reason,
			entrySetCodec.fieldOf("flags"), FlagsRequirement::flags,
			CHAINED_CODEC.fieldOf("chained"), FlagsRequirement::chained,
			FlagsRequirement::new
	);

	public FlagsRequirement(String reason, Set<Entry> flags, Requirement... chained) {
		this(reason, flags, List.of(chained));
	}

	@Override
	public MapCodec<? extends Requirement> codec() {
		return CODEC;
	}

	public static Builder builder(String reason) {
		return new FlagsRequirementBuilderImpl(reason);
	}

	public record Entry(AccessFlag flag, Mode mode) {
		public static final Codec<Entry> CODEC = CompositeCodec.of(
				SushiCodecs.ACCESS_FLAG.fieldOf("flag"), Entry::flag,
				Mode.CODEC.fieldOf("mode"), Entry::mode,
				Entry::new
		).asCodec();

		// these methods intentionally ignore mode so each flag can only appear once in sets and maps

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Entry that && this.flag == that.flag;
		}

		@Override
		public int hashCode() {
			return this.flag.hashCode();
		}

		public static Entry require(AccessFlag flag) {
			return new Entry(flag, Mode.REQUIRED);
		}

		public static Entry forbid(AccessFlag flag) {
			return new Entry(flag, Mode.FORBIDDEN);
		}

		public enum Mode {
			REQUIRED, FORBIDDEN;

			public static final Codec<Mode> CODEC = Codec.byName(Mode.class);
		}
	}

	/**
	 * A builder for a {@link FlagsRequirement}.
	 */
	public sealed interface Builder permits FlagsRequirementBuilderImpl {
		/**
		 * Require the given flag.
		 * @throws IllegalArgumentException if this flag has already been specified
		 */
		Builder require(AccessFlag flag) throws IllegalArgumentException;

		/**
		 * Forbid the given flag.
		 * @throws IllegalArgumentException if this flag has already been specified
		 */
		Builder forbid(AccessFlag flag) throws IllegalArgumentException;

		/**
		 * Add a new chained requirement.
		 */
		Builder chain(Requirement requirement);

		/**
		 * Build this builder into a new {@link FlagsRequirement}. This builder may be reused afterward.
		 */
		FlagsRequirement build();
	}
}
