package io.github.cichlidmc.sushi.impl.exp;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;

/**
 * Defines an expression in the code that can be targeted for modification.
 */
public interface ExpressionTarget {
	SimpleRegistry<MapCodec<? extends ExpressionTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapExpressionTargets);

	/**
	 * Find all expressions matching this target.
	 * Each returned instruction should be a location where, directly after,
	 * the top of the stack holds a non-void value.
	 */
	Collection<AbstractInsnNode> find(InsnList instructions);
}
