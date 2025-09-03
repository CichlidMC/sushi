package fish.cichlidmc.sushi.api.transform.inject;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.NameMapper;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.sushi.impl.transform.point.HeadInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.ReturnInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.TailInjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;
import java.util.function.Function;

/**
 * Defines locations inside a method body where an injection should take place.
 */
public interface InjectionPoint {
	SimpleRegistry<MapCodec<? extends InjectionPoint>> REGISTRY = SimpleRegistry.create(SushiInternals::boostrapInjectionPoints);
	Codec<InjectionPoint> CODEC = SushiInternals.make(() -> {
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
