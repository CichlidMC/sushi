package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.Transforms;
import org.jetbrains.annotations.ApiStatus;

/**
 * A transformer that always only applies one transform to one set of classes.
 */
public interface SimpleTransformer extends Transformer {
	@Override
	@ApiStatus.NonExtendable
	default void register(Transforms transforms) {
		transforms.register(this.classes(), this::apply);
	}

	void apply(TransformContext context) throws TransformException;

	ClassTarget classes();
}
