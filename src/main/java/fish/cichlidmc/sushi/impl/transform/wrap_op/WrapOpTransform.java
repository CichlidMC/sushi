package fish.cichlidmc.sushi.impl.transform.wrap_op;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.wrap_op.Operation;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WrapOpTransform extends HookingTransform {
	public static final MapCodec<WrapOpTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Hook.CODEC.fieldOf("wrapper"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			WrapOpTransform::new
	);

	private static final ClassDesc operationDesc = ClassDescs.of(Operation.class);

	private final ExpressionTarget target;

	public WrapOpTransform(MethodTarget method, Hook wrapper, ExpressionTarget target) {
		super(method, wrapper);
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<ExpressionTarget.Found> found = this.target.find(code);
		if (found.isEmpty())
			return;

		for (ExpressionTarget.Found target : found) {
			MethodTypeDesc desc = target.desc();

			List<ClassDesc> params = new ArrayList<>(desc.parameterList());
			params.add(operationDesc);
			DirectMethodHandleDesc hook = provider.get(desc.returnType(), params);

			String lambdaName = createLambdaName(context.transformerId());
			target.selection().extract(lambdaName, desc, (builder, operation) -> {
				// push the Operation to the stack
				operation.write(builder);
				// replace the original expression with the hook
				builder.invokestatic(hook.owner(), hook.methodName(), hook.invocationType(), hook.isOwnerInterface());
			});
		}
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}

	private static String createLambdaName(Id id) {
		return "wrap_operation$" + id.namespace + '$' + sanitizePath(id.path);
	}

	private static String sanitizePath(String path) {
		return path.replace('.', '_').replace('/', '_');
	}
}
