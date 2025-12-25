package fish.cichlidmc.sushi.api.match.inject;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.match.inject.builtin.HeadPointSelector;
import fish.cichlidmc.sushi.api.match.inject.builtin.ReturnPointSelector;
import fish.cichlidmc.sushi.api.match.inject.builtin.TailPointSelector;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// Defines locations inside a method body where an injection should take place.
public interface PointSelector {
	SimpleRegistry<MapCodec<? extends PointSelector>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<PointSelector> CODEC = Sushi.make(() -> {
		SimpleRegistry<PointSelector> specialCases = SimpleRegistry.create(Sushi.NAMESPACE);
		specialCases.register(Sushi.id("head"), HeadPointSelector.INSTANCE);
		specialCases.register(Sushi.id("tail"), TailPointSelector.INSTANCE);
		specialCases.register(Sushi.id("return"), ReturnPointSelector.ALL);
		return Codec.codecDispatch(REGISTRY.byIdCodec(), PointSelector::codec).withAlternative(specialCases.byIdCodec());
	});

	/// Find all points matching this selector in the given list of instructions.
	Collection<Point> find(TransformableCode code) throws TransformException;

	MapCodec<? extends PointSelector> codec();
}
