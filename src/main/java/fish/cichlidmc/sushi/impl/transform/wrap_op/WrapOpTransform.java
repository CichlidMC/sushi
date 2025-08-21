package fish.cichlidmc.sushi.impl.transform.wrap_op;

import fish.cichlidmc.sushi.api.BuiltInPhases;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.wrap_op.Operation;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.sushi.api.validation.MethodInfo;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public final class WrapOpTransform extends HookingTransform {
	public static final MapCodec<WrapOpTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			HOOK_CODEC.fieldOf("wrapper"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			WrapOpTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC, BuiltInPhases.WRAP_OPERATION);

	private static final ClassDesc operationDesc = ClassDescs.of(Operation.class);

	private final ExpressionTarget target;

	public WrapOpTransform(MethodTarget method, DirectMethodHandleDesc wrapper, ExpressionTarget target) {
		super(method, wrapper);
		this.target = target;
	}

	@Override
	protected void doApply(TransformContext context, TransformableCode code) throws TransformException {
		ExpressionTarget.Found found = this.target.find(code);
		if (found == null)
			return;

		MethodTypeDesc hookDesc = this.hook.invocationType();
		int last = hookDesc.parameterCount() - 1;
		MethodTypeDesc withoutOperation = hookDesc.dropParameterTypes(last, last + 1);
		if (!found.desc().equals(withoutOperation)) {
			throw new TransformException("Parameters " + withoutOperation.parameterList() + " do not match found stack types " + found.desc().parameterList());
		}

		for (Selection selection : found.selections()) {
			// TODO: replace this temporary garbage
			String lambdaName = "wrap_op_" + Math.abs(context.transformerId().hashCode());

			selection.extract(lambdaName, found.desc(), (builder, operation) -> {
				// push the Operation to the stack
				operation.write(builder);
				// replace the original expression with the hook
				builder.invokestatic(this.hook.owner(), this.hook.methodName(), hookDesc);
			});
		}
	}

	@Override
	protected void extraHookValidation(MethodInfo method) throws TransformException {
		super.extraHookValidation(method);
		List<ClassDesc> parameters = this.hook.invocationType().parameterList();
		if (parameters.isEmpty() || !parameters.getLast().equals(operationDesc)) {
			throw new TransformException("wrap_operation wrappers must take an Operation as their last parameter");
		}
	}

	@Override
	public String describe() {
		return "Wrap operation @ [" + this.target.describe() + "] in [" + this.method + "] calling [" + this.hook + ']';
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
