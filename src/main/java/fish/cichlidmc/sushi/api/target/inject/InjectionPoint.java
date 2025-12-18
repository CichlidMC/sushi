package fish.cichlidmc.sushi.api.target.inject;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.ReturnInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Collection;

/// Defines locations inside a method body where an injection should take place.
public interface InjectionPoint {
	SimpleRegistry<MapCodec<? extends InjectionPoint>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<InjectionPoint> CODEC = Sushi.make(() -> {
		SimpleRegistry<InjectionPoint> specialCases = SimpleRegistry.create(Sushi.NAMESPACE);
		specialCases.register(Sushi.id("head"), HeadInjectionPoint.INSTANCE);
		specialCases.register(Sushi.id("tail"), TailInjectionPoint.INSTANCE);
		specialCases.register(Sushi.id("return"), ReturnInjectionPoint.ALL);
		return Codec.codecDispatch(REGISTRY.byIdCodec(), InjectionPoint::codec).withAlternative(specialCases.byIdCodec());
	});

	/// Find all points to inject at in the given list of instructions.
	Collection<Point> find(TransformableCode code) throws TransformException;

	MapCodec<? extends InjectionPoint> codec();
}
