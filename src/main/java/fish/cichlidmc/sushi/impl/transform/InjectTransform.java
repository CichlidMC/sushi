package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.inject.Cancellation;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.Collection;

public final class InjectTransform extends HookingTransform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			HOOK_CODEC.fieldOf("hook"), inject -> inject.hook,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			InjectTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC);

	private final InjectionPoint point;

	private InjectTransform(MethodTarget method, DirectMethodHandleDesc hook, InjectionPoint point) {
		super(method, hook);
		this.point = point;
	}

	@Override
	protected void doApply(TransformContext context, TransformableCode code) throws TransformException {
		ClassDesc returnType = this.hook.invocationType().returnType();
		if (!ClassDescs.equals(returnType, Void.TYPE) && !ClassDescs.equals(returnType, Cancellation.class)) {
			throw new TransformException("Hook method must either return void or Cancellation: " + this.hook);
		}

		Collection<Point> found = this.point.find(code);
		if (found.isEmpty())
			return;

		for (Point point : found) {
			code.select().at(point).insertBefore(this::inject);
		}
	}

	private void inject(CodeBuilder builder) {
		// TODO: insert parameters
		builder.invokestatic(this.hook.owner(), this.hook.methodName(), this.hook.invocationType());

		if (!this.hook.invocationType().returnType().equals(ConstantDescs.CD_void)) {
			// TODO: use the Cancellation
			builder.pop();
		}
	}

	@Override
	public String describe() {
		return "Inject @ [" + this.point.describe() + "] in [" + this.method + "] calling [" + this.hook + ']';
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
