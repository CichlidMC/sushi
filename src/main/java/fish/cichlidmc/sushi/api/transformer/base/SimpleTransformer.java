package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.Transforms;
import org.jetbrains.annotations.ApiStatus;

/// A transformer that always applies only one transform to one set of classes.
public interface SimpleTransformer extends Transformer {
	@Override
	@ApiStatus.NonExtendable
	default void register(Transforms transforms) {
		transforms.register(this.classPredicate(), this::apply);
	}

	/// Apply the one and only transform defined by this transformer.
	void apply(TransformContext context) throws TransformException;

	/// @return a [ClassPredicate] matching the set of classes this transformer should apply to
	ClassPredicate classPredicate();
}
