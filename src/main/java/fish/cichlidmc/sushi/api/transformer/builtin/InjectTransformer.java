package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.match.point.PointTarget;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;
import fish.cichlidmc.sushi.api.transformer.infra.Cancellation;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.Collection;
import java.util.List;

/// Injects a hook callback into target methods.
/// The hook may optionally cancel the target method, returning a new value early.
public final class InjectTransformer extends HookingTransformer {
	public static final DualCodec<InjectTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), inject -> inject.classPredicate,
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			Slice.DEFAULTED_CODEC.fieldOf("slice"), inject -> inject.slice,
			Hook.CODEC.codec().fieldOf("hook"), inject -> inject.hook,
			Codec.BOOL.optional(false).fieldOf("cancellable"), inject -> inject.cancellable,
			PointTarget.CODEC.fieldOf("point"), inject -> inject.target,
			InjectTransformer::new
	);

	private static final ClassDesc cancellationDesc = ClassDescs.of(Cancellation.class);

	private final boolean cancellable;
	private final PointTarget target;

	public InjectTransformer(ClassPredicate classes, MethodTarget method, Slice slice, Hook hook, boolean cancellable, PointTarget target) {
		super(classes, method, slice, hook);
		this.cancellable = cancellable;
		this.target = target;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException {
		Collection<Point> found = this.target.find(code);

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
		ContextParameter.with(params, builder, b -> Instructions.invokeMethod(b, desc));

		if (!this.cancellable)
			return;

		TypeKind returnTypeKind = TypeKind.from(targetReturnType);

		// IFNONNULL consumes the reference, dupe it
		builder.dup();
		builder.ifThenElse(Opcode.IFNONNULL, block -> {
			if (returnTypeKind == TypeKind.VOID) {
				block.pop();
				block.return_();
			} else {
				block.getfield(cancellationDesc, "value", ConstantDescs.CD_Object);

				// checkcast, and unbox if needed
				if (targetReturnType.isPrimitive()) {
					Instructions.unboxChecked(block, targetReturnType);
				} else {
					block.checkcast(targetReturnType);
				}

				block.return_(returnTypeKind);
			}
		}, CodeBuilder::pop);
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
