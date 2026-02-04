package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.match.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.List;
import java.util.function.UnaryOperator;

/// Modifies some targeted expression with a hook callback that acts similar to a [UnaryOperator].
public final class ModifyExpressionTransformer extends HookingTransformer {
	public static final DualCodec<ModifyExpressionTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), transform -> transform.classPredicate,
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), transform -> transform.slice,
			Hook.CODEC.codec().fieldOf("hook"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("expression"), transform -> transform.target,
			ModifyExpressionTransformer::new
	);

	private final ExpressionTarget target;

	public ModifyExpressionTransformer(ClassPredicate predicate, MethodTarget method, Slice slice, Hook modifier, ExpressionTarget target) {
		super(predicate, method, slice, modifier);
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		for (ExpressionSelector.Found found : this.target.find(code)) {
			if (!(found.delta() instanceof StackDelta.MethodLike delta)) {
				throw new TransformException("Can only modify expressions that push a single value");
			} else if (delta.singlePushed().isEmpty()) {
				throw new TransformException("Cannot modify an expression that pushes nothing");
			}

			ClassDesc modifierType = delta.singlePushed().get();
			DirectMethodHandleDesc hook = provider.get(modifierType, List.of(modifierType));

			Selection selection = found.selection();
			Point point = selection.end();

			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			selection.insertAfter(builder -> this.inject(builder, modifierType, hook, params));
		}
	}

	private void inject(CodeBuilder builder, ClassDesc modifierType, DirectMethodHandleDesc hook, List<ContextParameter.Prepared> params) {
		ContextParameter.with(params, builder, b -> {
			Instructions.invokeMethod(b, hook);
			if (!modifierType.equals(hook.invocationType().returnType())) {
				// coerced to a weaker type, add a checkcast
				b.checkcast(modifierType);
			}
		});
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
