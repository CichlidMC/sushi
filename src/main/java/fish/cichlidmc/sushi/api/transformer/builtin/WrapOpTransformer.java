package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.match.MethodTarget;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Wraps an operation, passing it to a hook callback as a lambda.
public final class WrapOpTransformer extends HookingTransformer {
	public static final DualCodec<WrapOpTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), transform -> transform.classPredicate,
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), transform -> transform.slice,
			Hook.CODEC.codec().fieldOf("wrapper"), transform -> transform.hook,
			ExpressionSelector.CODEC.fieldOf("selector"), transform -> transform.selector,
			WrapOpTransformer::new
	);

	private final ExpressionSelector selector;

	public WrapOpTransformer(ClassPredicate classes, MethodTarget method, Slice slice, Hook wrapper, ExpressionSelector selector) {
		super(classes, method, slice, wrapper);
		this.selector = selector;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<ExpressionSelector.Found> found = this.selector.find(code);
		if (found.isEmpty())
			return;

		for (ExpressionSelector.Found target : found) {
			Point point = target.selection().start();

			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			MethodTypeDesc desc = target.desc();

			List<ClassDesc> hookParams = new ArrayList<>(desc.parameterList());
			hookParams.add(ClassDescs.OPERATION);

			DirectMethodHandleDesc hook = provider.get(desc.returnType(), hookParams);

			String lambdaName = createLambdaName(context.transformerId());
			target.selection().extract(lambdaName, desc, builder -> ContextParameter.with(params, builder, b -> {
				// replace the original expression with the hook
				Instructions.invokeMethod(b, hook);
			}));
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
