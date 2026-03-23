package fish.cichlidmc.sushi.api.codec;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.codec.ConstantDescCodec;
import fish.cichlidmc.sushi.impl.codec.TypedDirectMethodHandleDesc;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.lang.Enum.EnumDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;

/// Additional Codecs used by Sushi.
public final class SushiCodecs {
	public static final Codec<Integer> NON_NEGATIVE_INT = Codec.INT.validate(i -> {
		if (i < 0) {
			return Result.error("Value must be >=0: " + i);
		} else {
			return Result.success(i);
		}
	});

	public static final Codec<AccessFlag> ACCESS_FLAG = Codec.byName(AccessFlag.class);

	public static final DualCodec<MethodTypeDesc> METHOD_TYPE = CompositeCodec.of(
			ClassDescs.ANY_CODEC.fieldOf("return"), MethodTypeDesc::returnType,
			ClassDescs.ANY_CODEC.listOf().fieldOf("params"), MethodTypeDesc::parameterList,
			MethodTypeDesc::of
	);

	public static final Codec<DirectMethodHandleDesc> METHOD_HANDLE = TypedDirectMethodHandleDesc.CODEC.xmap(
			TypedDirectMethodHandleDesc::unwrap, TypedDirectMethodHandleDesc::wrap
	);

	public static final DualCodec<EnumDesc<?>> ENUM_DESC = CompositeCodec.of(
			ClassDescs.CLASS_CODEC.fieldOf("class"), EnumDesc::constantType,
			Codec.STRING.fieldOf("name"), EnumDesc::constantName,
			EnumDesc::of
	);

	/// A codec that can handle most [ConstantDesc]s. Will fail for custom [DynamicConstantDesc]s.
	public static final Codec<ConstantDesc> CONSTANT = ConstantDescCodec.INSTANCE;

	/// Create a codec for a strict set of `T`. Duplicate entries will cause an error on decode.
	/// @param setFactory factory that will be invoked to create a new set
	public static <T> Codec<Set<T>> setOf(Codec<T> codec, Supplier<Set<T>> setFactory) {
		return codec.listOf().comapFlatMap(
				list -> {
					Set<T> set = setFactory.get();
					for (T t : list) {
						if (!set.add(t)) {
							return Result.error("Duplicate set entry: " + t);
						}
					}
					return Result.success(set);
				},
				ArrayList::new
		);
	}

	private SushiCodecs() {}
}
