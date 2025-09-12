package fish.cichlidmc.sushi.api.codec;

import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;

/**
 * Additional Codecs used by Sushi.
 */
public final class SushiCodecs {
	public static final Codec<Integer> NON_NEGATIVE_INT = Codec.INT.validate(i -> {
		if (i < 0) {
			return CodecResult.error("Value must be >=0: " + i);
		} else {
			return CodecResult.success(i);
		}
	});

	private SushiCodecs() {
	}
}
