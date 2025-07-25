package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;

import java.util.List;

public abstract class CodeTargetingTransform implements Transform {
	protected final MethodTarget method;

	protected CodeTargetingTransform(MethodTarget method) {
		this.method = method;
	}

	@Override
	public final void apply(TransformContext context) throws TransformException {
		List<TransformableMethod> methods = this.method.findOrThrow(context.clazz());

		for (TransformableMethod method : methods) {
			TransformableCode code = method.code().orElseThrow(() -> new TransformException("Target method has no code"));
			this.apply(context, code);
		}
	}

	protected abstract void apply(TransformContext context, TransformableCode code) throws TransformException;
}
