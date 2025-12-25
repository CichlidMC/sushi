package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.MethodTarget;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;

import java.util.List;

/// A transformer that targets the code of methods.
public abstract class CodeTargetingTransformer implements SimpleTransformer {
	protected final ClassPredicate classPredicate;
	protected final MethodTarget method;
	protected final Slice slice;

	protected CodeTargetingTransformer(ClassPredicate predicate, MethodTarget method, Slice slice) {
		this.classPredicate = predicate;
		this.method = method;
		this.slice = slice;
	}

	@Override
	public final void apply(TransformContext context) throws TransformException {
		List<TransformableMethod> methods = this.method.find(context.target());

		for (TransformableMethod method : methods) {
			Details.with("Method", method, TransformException::new, () -> {
				TransformableCode code = method.code()
						.map(this.slice::apply)
						.orElseThrow(() -> new TransformException("Target method has no code"));

				this.apply(context, code);
			});
		}
	}

	@Override
	public final ClassPredicate classPredicate() {
		return this.classPredicate;
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
