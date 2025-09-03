package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;

public final class ModifyExpressionTransform extends HookingTransform {
	public static final MapCodec<ModifyExpressionTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			HOOK_CODEC.fieldOf("modifier"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			ModifyExpressionTransform::new
	);

	private final ExpressionTarget target;

	private ModifyExpressionTransform(MethodTarget method, DirectMethodHandleDesc modifier, ExpressionTarget target) {
		super(method, modifier);
		this.target = target;
	}

	@Override
	protected void doApply(TransformContext context, TransformableCode code) throws TransformException {
		ExpressionTarget.Found found = this.target.find(code);
		if (found == null)
			return;

		ClassDesc returnType = this.hook.invocationType().returnType();
		if (!returnType.equals(found.desc().returnType())) {
			throw new TransformException("Found target and modifier have incompatible types: " + returnType + " / " + found.desc().returnType());
		}

		ClassDesc[] params = this.hook.invocationType().parameterArray();
		if (params.length != 1) {
			throw new TransformException("Modifier method must take one parameter, the original value");
		}

		if (!returnType.equals(params[0])) {
			throw new TransformException("Modifier method must take and return the same type: " + returnType);
		}

		found.selections().forEach(selection -> selection.insertAfter(this::inject));
	}

	private void inject(CodeBuilder builder) {
		// TODO: insert parameters
		builder.invokestatic(this.hook.owner(), this.hook.methodName(), this.hook.invocationType());
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}
}
