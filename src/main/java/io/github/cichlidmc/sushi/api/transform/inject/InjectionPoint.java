package io.github.cichlidmc.sushi.api.transform.inject;

import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.util.NameMapper;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.transform.point.HeadInjectionPoint;
import io.github.cichlidmc.sushi.impl.transform.point.ReturnInjectionPoint;
import io.github.cichlidmc.sushi.impl.transform.point.TailInjectionPoint;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Locale;
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
	 * Find all instructions to use as injection targets.
	 * An injection will be inserted right before each returned instruction.
	 */
	Collection<? extends AbstractInsnNode> find(InsnList instructions);

	default Shift shift() {
		return Shift.BEFORE;
	}

	/**
	 * @return a human-readable, single-line description of this point.
	 * <p>
	 * Examples: {@code head}, {@code all returns}, {@code before invoke of com.example.MyClass.myMethod}
	 * @see Transform#describe()
	 */
	String describe();

	MapCodec<? extends InjectionPoint> codec();

	enum Shift {
		BEFORE, AFTER;

		public static final Codec<Shift> CODEC = Codec.byName(Shift.class, shift -> shift.name);

		public final String name = this.name().toLowerCase(Locale.ROOT);
	}
}
