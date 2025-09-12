package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.tinycodecs.Codec;

import java.util.Locale;

/**
 * An offset in bytecode, used in several different contexts.
 */
public enum Offset {
	BEFORE, AFTER;

	public static final Codec<Offset> CODEC = Codec.byName(Offset.class, offset -> offset.name().toLowerCase(Locale.ROOT));
}
