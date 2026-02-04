package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.OperationInfra;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.ArrayList;
import java.util.List;

/// Wraps an operation, passing it to a hook callback as a lambda.
public final class WrapOpTransformer extends HookingTransformer {
	public static final DualCodec<WrapOpTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), transform -> transform.classPredicate,
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), transform -> transform.slice,
			Hook.CODEC.codec().fieldOf("hook"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("expression"), transform -> transform.target,
			WrapOpTransformer::new
	);

	private final ExpressionTarget target;

	public WrapOpTransformer(ClassPredicate classes, MethodTarget method, Slice slice, Hook wrapper, ExpressionTarget target) {
		super(classes, method, slice, wrapper);
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		for (ExpressionSelector.Found found : this.target.find(code)) {
			if (!(found.delta() instanceof StackDelta.MethodLike delta)) {
				throw new TransformException("Cannot wrap an operation that pushes more than one value");
			}

			ClassDesc hookReturnType = delta.pushedOrVoid();
			Point point = found.selection().start();

			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			List<ClassDesc> hookParams = new ArrayList<>(delta.popped());
			hookParams.add(OperationInfra.OPERATION_DESC);

			DirectMethodHandleDesc hook = provider.get(hookReturnType, hookParams);

			String lambdaName = context.target().createUniqueMethodName("wrap_operation", context.transformerId());
			found.selection().extract(lambdaName, delta, builder -> ContextParameter.with(params, builder, b -> {
				// replace the original expression with the hook
				Instructions.invokeMethod(b, hook);
				if (!hook.invocationType().returnType().equals(hookReturnType)) {
					// coerced to a weaker type, checkcast
					b.checkcast(hookReturnType);
				}
			}));
		}
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
