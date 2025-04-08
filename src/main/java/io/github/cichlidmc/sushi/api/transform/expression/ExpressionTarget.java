package io.github.cichlidmc.sushi.api.transform.expression;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
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
	Collection<AbstractInsnNode> find(InsnList instructions);

	MapCodec<? extends ExpressionTarget> codec();
}
