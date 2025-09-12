package fish.cichlidmc.sushi.api.target.inject;

import fish.cichlidmc.sushi.api.codec.NameMapper;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.ReturnInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.impl.registry.SushiBootstraps;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;
import java.util.function.Function;

/**
 * Defines locations inside a method body where an injection should take place.
 */
public interface InjectionPoint {
	SimpleRegistry<MapCodec<? extends InjectionPoint>> REGISTRY = SimpleRegistry.create(SushiBootstraps::boostrapInjectionPoints);
	Codec<InjectionPoint> CODEC = SushiBootstraps.make(() -> {
		NameMapper<InjectionPoint> specialCases = new NameMapper<>();
		specialCases.put("head", HeadInjectionPoint.INSTANCE);
		specialCases.put("tail", TailInjectionPoint.INSTANCE);
		specialCases.put("return", ReturnInjectionPoint.ALL);
		return REGISTRY.byIdCodec().dispatch(InjectionPoint::codec, Function.identity())
				.withAlternative(specialCases.codec);
	});

	/**
	 * Find all points to inject at in the given list of instructions.
	 */
	Collection<Point> find(TransformableCode code) throws TransformException;

	MapCodec<? extends InjectionPoint> codec();
}
