package fish.cichlidmc.sushi.impl.codec;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.Decoder;
import fish.cichlidmc.tinyjson.value.JsonValue;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public enum ConstantDescCodec implements Codec<ConstantDesc> {
	INSTANCE;

	// will be tried in order, must be most strict first
	private static final List<Decoder<? extends ConstantDesc>> decoders = List.of(
			ClassDescs.ANY_CODEC, SushiCodecs.METHOD_HANDLE,
			SushiCodecs.METHOD_TYPE.codec(), SushiCodecs.ENUM_DESC.codec(),
			Codec.STRING, Codec.INT, Codec.LONG, Codec.FLOAT, Codec.DOUBLE
	);

	@Override
	public Result<? extends JsonValue> encode(ConstantDesc value) {
		return switch (value) {
			case ClassDesc clazz -> ClassDescs.ANY_CODEC.encode(clazz);
			case MethodHandleDesc handle -> switch (handle) {
				case DirectMethodHandleDesc direct -> SushiCodecs.METHOD_HANDLE.encode(direct);
				default -> Result.error("Cannot encode a non-direct MethodHandle");
			};
			case MethodTypeDesc type -> SushiCodecs.METHOD_TYPE.codec().encode(type);
			case Double d -> Codec.DOUBLE.encode(d);
			case Float f -> Codec.FLOAT.encode(f);
			case Integer i -> Codec.INT.encode(i);
			case Long l -> Codec.LONG.encode(l);
			case String s -> Codec.STRING.encode(s);
			case EnumDesc<?> desc -> SushiCodecs.ENUM_DESC.codec().encode(desc);
			case DynamicConstantDesc<?> _ -> Result.error("Unsupported DynamicConstantDesc");
		};
	}

	@Override
	public Result<ConstantDesc> decode(JsonValue json) {
		for (Decoder<? extends ConstantDesc> decoder : decoders) {
			if (decoder.decode(json) instanceof Result.Success<? extends ConstantDesc> success) {
				return Result.success(success.value());
			}
		}

		return Result.error("All decoders failed");
	}
}
