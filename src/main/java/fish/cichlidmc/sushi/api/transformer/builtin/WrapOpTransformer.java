package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wraps an operation, passing it to a hook callback as a lambda.
 */
public final class WrapOpTransformer extends HookingTransformer {
	public static final DualCodec<WrapOpTransformer> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("classes"), transform -> transform.classes,
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), transform -> transform.slice,
			Hook.CODEC.codec().fieldOf("wrapper"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			WrapOpTransformer::new
	);

	private static final ClassDesc operationDesc = ClassDescs.of(Operation.class);

	private final ExpressionTarget target;

	public WrapOpTransformer(ClassTarget classes, MethodTarget method, Slice slice, Hook wrapper, ExpressionTarget target) {
		super(classes, method, slice, wrapper);
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<ExpressionTarget.Found> found = this.target.find(code);
		if (found.isEmpty())
			return;

		for (ExpressionTarget.Found target : found) {
			Point point = target.selection().start();

			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			MethodTypeDesc desc = target.desc();

			List<ClassDesc> hookParams = new ArrayList<>(desc.parameterList());
			hookParams.add(operationDesc);

			DirectMethodHandleDesc hook = provider.get(desc.returnType(), hookParams);

			String lambdaName = createLambdaName(context.transformerId());
			target.selection().extract(lambdaName, desc, (builder, operation) -> {
				// push the Operation to the stack
				operation.write(builder);

				params.forEach(param -> param.pre(builder));

				// replace the original expression with the hook
				builder.invokestatic(hook.owner(), hook.methodName(), hook.invocationType(), hook.isOwnerInterface());

				params.forEach(param -> param.post(builder));
			});
		}
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}

	private static String createLambdaName(Id id) {
		return "wrap_operation$" + id.namespace + '$' + sanitizePath(id.path);
	}

	private static String sanitizePath(String path) {
		return path.replace('.', '_').replace('/', '_');
	}
}
