package fish.cichlidmc.sushi.api.transform.base;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;

import java.util.List;

/**
 * A transform that targets the code of methods.
 */
public abstract class CodeTargetingTransform implements Transform {
	protected final MethodTarget method;

	protected CodeTargetingTransform(MethodTarget method) {
		this.method = method;
	}

	@Override
	public final void apply(TransformContext context) throws TransformException {
		List<TransformableMethod> methods = this.method.find(context.clazz());

		for (TransformableMethod method : methods) {
			TransformableCode code = method.code().orElseThrow(() -> new TransformException("Target method has no code"));
			this.apply(context, code);
		}
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
