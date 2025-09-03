package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.transform.CodeTargetingTransform;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.inject.Cancellation;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.sushi.api.validation.MethodInfo;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.TypeKind;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Collection;
import java.util.List;

public final class InjectTransform extends CodeTargetingTransform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), inject -> inject.method,
			Hook.CODEC.fieldOf("hook"), inject -> inject.hook,
			Codec.BOOL.optional(false).fieldOf("cancellable"), inject -> inject.cancellable,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			InjectTransform::new
	);

	private static final ClassDesc cancellationDesc = ClassDescs.of(Cancellation.class);

	private final Hook hook;
	private final boolean cancellable;
	private final InjectionPoint point;

	private InjectTransform(MethodTarget method, Hook hook, boolean cancellable, InjectionPoint point) {
		super(method);
		this.hook = hook;
		this.cancellable = cancellable;
		this.point = point;
	}

	@Override
	protected void apply(TransformContext context, TransformableCode code) throws TransformException {
		ClassDesc returnType = this.cancellable ? cancellationDesc : ConstantDescs.CD_void;
		DirectMethodHandleDesc desc = this.hook.createDesc(returnType);

		context.validation().ifPresent(validation -> {
			MethodInfo info = validation.findMethod(desc).orElseThrow(() -> new TransformException("Hook method not found: " + desc));
			if (!info.flags().contains(AccessFlag.PUBLIC) || !info.flags().contains(AccessFlag.STATIC)) {
				throw new TransformException("Hook method is not public and static: " + desc);
			}
		});

		Collection<Point> found = this.point.find(code);
		if (found.isEmpty())
			return;

		for (Point point : found) {
			List<ContextParameter.Prepared> params = this.hook.params.stream()
					.map(param -> param.prepare(context, code, point))
					.toList();

			code.select().at(point).insertBefore(builder -> {
				ClassDesc targetReturnType = code.owner().returnType();
				this.inject(builder, targetReturnType, desc, params);
			});
		}
	}

	private void inject(CodeBuilder builder, ClassDesc targetReturnType, DirectMethodHandleDesc desc, List<ContextParameter.Prepared> params) {
		params.forEach(param -> param.pre(builder));
		builder.invokestatic(desc.owner(), desc.methodName(), desc.invocationType());
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

	public record Hook(ClassDesc clazz, boolean classIsInterface, String name, List<ContextParameter> params) {
		public static final MapCodec<Hook> MAP_CODEC = CompositeCodec.of(
				ClassDescs.CLASS_CODEC.fieldOf("class"), Hook::clazz,
				Codec.BOOL.optional(false).fieldOf("class_is_interface"), Hook::classIsInterface,
				Codec.STRING.fieldOf("name"), Hook::name,
				ContextParameter.CODEC.listOf().optional(List.of()).fieldOf("parameters"), Hook::params,
				Hook::new
		);
		public static final Codec<Hook> CODEC = MAP_CODEC.asCodec();

		public DirectMethodHandleDesc createDesc(ClassDesc returnType) {
			ClassDesc[] params = this.params.stream().map(ContextParameter::type).toArray(ClassDesc[]::new);
			MethodTypeDesc desc = MethodTypeDesc.of(returnType, params);
			return MethodHandleDesc.ofMethod(this.invokeKind(), this.clazz, this.name, desc);
		}

		public DirectMethodHandleDesc.Kind invokeKind() {
			return this.classIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
		}
	}
}
