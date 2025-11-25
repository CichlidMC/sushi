package fish.cichlidmc.sushi.api.codec;

import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.CodecResult;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.glavo.classfile.AccessFlag;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;

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
	public static final Codec<JsonValue> PASSTHROUGH = Codec.of(
			value -> CodecResult.success(value.copy()),
			value -> CodecResult.success(value.copy())
	);
	public static final DualCodec<MethodTypeDesc> METHOD_DESC = CompositeCodec.of(
			ClassDescs.ANY_CODEC.fieldOf("return"), MethodTypeDesc::returnType,
			ClassDescs.ANY_CODEC.listOf().fieldOf("params"), MethodTypeDesc::parameterList,
			MethodTypeDesc::of
	);
	public static final Codec<AccessFlag> ACCESS_FLAG = Codec.byName(AccessFlag.class);

	/**
	 * Create a codec for a strict set of {@code T}. Duplicate entries will cause an error on decode.
	 * @param setFactory factory that will be invoked to create a new set
	 */
	public static <T> Codec<Set<T>> setOf(Codec<T> codec, Supplier<Set<T>> setFactory) {
		return codec.listOf().comapFlatMap(
				list -> {
					Set<T> set = setFactory.get();
					for (T t : list) {
						if (!set.add(t)) {
							return CodecResult.error("Duplicate set entry: " + t);
						}
					}
					return CodecResult.success(set);
				},
				ArrayList::new
		);
	}

	private SushiCodecs() {
	}
}
