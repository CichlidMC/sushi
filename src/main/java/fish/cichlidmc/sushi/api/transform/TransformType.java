package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.BuiltInPhases;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.tinycodecs.map.MapCodec;

public final class TransformType {
	public static final SimpleRegistry<TransformType> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapTransforms);

	public final MapCodec<? extends Transform> codec;
	public final int defaultPhase;

	public TransformType(MapCodec<? extends Transform> codec, int defaultPhase) {
		this.codec = codec;
		this.defaultPhase = defaultPhase;
	}

	public TransformType(MapCodec<? extends Transform> codec) {
		this(codec, BuiltInPhases.DEFAULT);
	}
}
