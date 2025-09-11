package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.Collection;
import java.util.List;

public final class ModifyExpressionTransform extends HookingTransform {
	public static final MapCodec<ModifyExpressionTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			Hook.CODEC.fieldOf("modifier"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			ModifyExpressionTransform::new
	);

	private final ExpressionTarget target;

	private ModifyExpressionTransform(MethodTarget method, Hook modifier, ExpressionTarget target) {
		super(method, modifier);
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
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}
}
