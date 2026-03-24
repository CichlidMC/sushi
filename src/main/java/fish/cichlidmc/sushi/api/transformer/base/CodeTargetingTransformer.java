package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;

/// A transformer that targets the code of methods.
public abstract class CodeTargetingTransformer extends MethodTargetingTransformer {
	protected CodeTargetingTransformer(ClassPredicate predicate, MethodTarget method) {
		super(predicate, method);
	}

	@Override
	protected final void apply(TransformContext context, TransformableMethod method) throws TransformException {
		TransformableCode code = method.code().orElseThrow(() -> new TransformException("Target method has no code"));
		this.apply(context, code);
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
