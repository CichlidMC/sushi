package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;

/// A transformer that targets the code of methods.
public abstract class CodeTargetingTransformer extends MethodTargetingTransformer {
	protected final Slice slice;

	protected CodeTargetingTransformer(ClassPredicate predicate, MethodTarget method, Slice slice) {
		super(predicate, method);
		this.slice = slice;
	}

	@Override
	protected final void apply(TransformContext context, TransformableMethod method) throws TransformException {
		TransformableCode code = method.code()
				.map(this.slice::apply)
				.orElseThrow(() -> new TransformException("Target method has no code"));

		this.apply(context, code);
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
