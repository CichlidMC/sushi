package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.Transformer;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * Context about the currently occurring transformation.
 */
@ApiStatus.NonExtendable
public interface TransformContext {
	/**
	 * @return the {@link ClassNode} currently being transformed.
	 */
	ClassNode node();

	/**
	 * Generates a new name for a method, guaranteed to be unique.
	 * Format: {@code "<prefix>$$<current_transformer_id>$$<number>}
	 */
	String generateUniqueMethodName(String prefix);

	/**
	 * A list of all {@link Transformer}s that have already been applied, in order.
	 * A transformer being applied doesn't guarantee it actually did anything.
	 */
	List<Transformer> applied();

	/**
	 * @return the {@link Transformer} that is in the middle of being applied.
	 */
	Transformer applying();

	/**
	 * @return a list of all {@link Transformer}s that will be applied next, in order.
	 */
	List<Transformer> toApply();
}
