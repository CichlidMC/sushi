package io.github.cichlidmc.sushi.api.transform;

import io.github.cichlidmc.sushi.api.BuiltInPhases;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.tinycodecs.map.MapCodec;

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
