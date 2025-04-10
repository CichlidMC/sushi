package io.github.cichlidmc.sushi.api.transform.expression;

import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;

/**
 * Defines an expression in a method body that can be targeted for modification.
 */
public interface ExpressionTarget {
	SimpleRegistry<MapCodec<? extends ExpressionTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapExpressionTargets);
	Codec<ExpressionTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionTarget::codec);

	/**
	 * Find all expressions matching this target.
	 * Each returned instruction should be a location where, directly after, the top of the stack holds a non-void value.
	 */
	Collection<FoundExpressionTarget> find(InsnList instructions);

	/**
	 * @return a human-readable, single-line description of this target.
	 * <p>
	 * Examples: {@code all invokes of com.example.MyClass.myMethod}, {@code read #3 of com.example.MyClass.myField}
	 * @see Transform#describe()
	 */
	String describe();

	MapCodec<? extends ExpressionTarget> codec();
}
