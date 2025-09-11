package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.inject.Cancellation;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.TypeKind;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.Collection;
import java.util.List;

public final class InjectTransform extends HookingTransform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			Hook.CODEC.fieldOf("hook"), inject -> inject.hook,
			Codec.BOOL.optional(false).fieldOf("cancellable"), inject -> inject.cancellable,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			InjectTransform::new
	);

	private static final ClassDesc cancellationDesc = ClassDescs.of(Cancellation.class);

	private final boolean cancellable;
	private final InjectionPoint point;

	private InjectTransform(MethodTarget method, Hook hook, boolean cancellable, InjectionPoint point) {
		super(method, hook);
		this.cancellable = cancellable;
		this.point = point;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<Point> found = this.point.find(code);
		if (found.isEmpty())
			return;

		ClassDesc returnType = this.cancellable ? cancellationDesc : ConstantDescs.CD_void;
		DirectMethodHandleDesc hook = provider.get(returnType, List.of());

		for (Point point : found) {
			List<ContextParameter.Prepared> params = this.hook.params().stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			code.select().at(point).insertBefore(builder -> {
				ClassDesc targetReturnType = code.owner().returnType();
				this.inject(builder, targetReturnType, hook, params);
			});
		}
	}

	private void inject(CodeBuilder builder, ClassDesc targetReturnType, DirectMethodHandleDesc desc, List<ContextParameter.Prepared> params) {
		params.forEach(param -> param.pre(builder));
		builder.invokestatic(desc.owner(), desc.methodName(), desc.invocationType(), desc.isOwnerInterface());
		params.forEach(param -> param.post(builder));

		if (!this.cancellable)
			return;

		TypeKind returnTypeKind = TypeKind.from(targetReturnType);

		// IFNONNULL consumes the reference, dupe it
		builder.dup();
		builder.ifThenElse(Opcode.IFNONNULL, block -> {
			if (returnTypeKind == TypeKind.VoidType) {
				block.pop();
				block.return_();
			} else {
				block.getfield(cancellationDesc, "value", ConstantDescs.CD_Object);
				Instructions.maybeUnbox(block, targetReturnType);
				block.returnInstruction(returnTypeKind);
			}
		}, CodeBuilder::pop);
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}
}
