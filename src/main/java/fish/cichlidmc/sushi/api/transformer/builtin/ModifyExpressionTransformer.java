package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.target.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/// Modifies some targeted expression with a hook callback that acts similar to a [UnaryOperator].
public final class ModifyExpressionTransformer extends HookingTransformer {
	public static final DualCodec<ModifyExpressionTransformer> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("classes"), transform -> transform.classes,
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), transform -> transform.slice,
			Hook.CODEC.codec().fieldOf("modifier"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("expression"), transform -> transform.target,
			ModifyExpressionTransformer::new
	);

	private final ExpressionTarget target;

	public ModifyExpressionTransformer(ClassTarget classTarget, MethodTarget method, Slice slice, Hook modifier, ExpressionTarget target) {
		super(classTarget, method, slice, modifier);
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<ExpressionTarget.Found> found = this.target.find(code);
		if (found.isEmpty())
			return;

		for (ExpressionTarget.Found target : found) {
			ClassDesc modifierType = target.desc().returnType();
			DirectMethodHandleDesc hook = provider.get(modifierType, List.of(modifierType));

			Selection selection = target.selection();
			Point point = selection.end();

			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			selection.insertAfter(builder -> this.inject(builder, hook, params));
		}
	}

	private void inject(CodeBuilder builder, DirectMethodHandleDesc desc, List<ContextParameter.Prepared> params) {
		params.forEach(param -> param.pre(builder));
		builder.invokestatic(desc.owner(), desc.methodName(), desc.invocationType(), desc.isOwnerInterface());
		params.forEach(param -> param.post(builder));
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
