package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transformer.RegisteredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transform;

// intentionally has identity semantics
@SuppressWarnings("ClassCanBeRecord")
public final class PreparedTransform {
	public final RegisteredTransformer owner;
	public final ClassTarget target;
	public final Transform transform;

	public PreparedTransform(RegisteredTransformer owner, ClassTarget target, Transform transform) {
		this.owner = owner;
		this.target = target;
		this.transform = transform;
	}
}
