package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.transformer.RegisteredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transform;

// intentionally has identity semantics
@SuppressWarnings("ClassCanBeRecord")
public final class PreparedTransform {
	public final RegisteredTransformer owner;
	public final ClassPredicate target;
	public final Transform transform;

	public PreparedTransform(RegisteredTransformer owner, ClassPredicate target, Transform transform) {
		this.owner = owner;
		this.target = target;
		this.transform = transform;
	}
}
