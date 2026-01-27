package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;

import java.util.Collection;

/// A transformer that targets the methods of a class.
public abstract class MethodTargetingTransformer implements SimpleTransformer {
	protected final ClassPredicate classPredicate;
	protected final MethodTarget method;

	protected MethodTargetingTransformer(ClassPredicate predicate, MethodTarget method) {
		this.classPredicate = predicate;
		this.method = method;
	}

	@Override
	public final void apply(TransformContext context) throws TransformException {
		Collection<TransformableMethod> methods = this.method.find(context.target());

		for (TransformableMethod method : methods) {
			Details.with("Method", method, TransformException::new, () -> this.apply(context, method));
		}
	}

	@Override
	public final ClassPredicate classPredicate() {
		return this.classPredicate;
	}

	protected abstract void apply(TransformContext context, TransformableMethod method) throws TransformException;
}
