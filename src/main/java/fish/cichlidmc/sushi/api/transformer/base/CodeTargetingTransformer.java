package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;

import java.util.List;

/// A transformer that targets the code of methods.
public abstract class CodeTargetingTransformer implements SimpleTransformer {
	protected final ClassTarget classes;
	protected final MethodTarget method;
	protected final Slice slice;

	protected CodeTargetingTransformer(ClassTarget classes, MethodTarget method, Slice slice) {
		this.classes = classes;
		this.method = method;
		this.slice = slice;
	}

	@Override
	public final void apply(TransformContext context) throws TransformException {
		List<TransformableMethod> methods = this.method.find(context.clazz());

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
	public final ClassTarget classes() {
		return this.classes;
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
