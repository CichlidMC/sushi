package fish.cichlidmc.sushi.impl.transform.wrap_op;

import fish.cichlidmc.sushi.api.BuiltInPhases;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.wrap_op.Operation;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.sushi.impl.runtime.WrapOpValidation;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.AccessFlags;
import org.glavo.classfile.TypeKind;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class WrapOpTransform extends HookingTransform {
	public static final ClassDesc VALIDATION_DESC = ClassDescs.of(WrapOpValidation.class);
	public static final String CHECK_COUNT = "checkCount";
	public static final MethodTypeDesc CHECK_COUNT_DESC = MethodTypeDesc.of(
			ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_int
	);

	/**
	 * @see LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)
	 */
	public static final DirectMethodHandleDesc LMF = ConstantDescs.ofCallsiteBootstrap(
			ClassDescs.of(LambdaMetafactory.class), "metafactory", ConstantDescs.CD_CallSite,
			// args
			ConstantDescs.CD_MethodType, ConstantDescs.CD_MethodHandle, ConstantDescs.CD_MethodType
	);

	public static final MethodTypeDesc OPERATION_TYPE = MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object.arrayType());

	// no captured args, just returns an Operation
	public static final MethodTypeDesc OPERATION_LAMBDA_FACTORY = MethodTypeDesc.of(ClassDescs.of(Operation.class));

	public static final MapCodec<WrapOpTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			HOOK_CODEC.fieldOf("wrapper"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			WrapOpTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC, BuiltInPhases.WRAP_OPERATION);

	private final ExpressionTarget target;

	public WrapOpTransform(MethodTarget method, DirectMethodHandleDesc wrapper, ExpressionTarget target) {
		super(method, wrapper);
		this.target = target;
	}

	@Override
	protected void doApply(TransformContext context, TransformableCode code) throws TransformException {
		ExpressionTarget.Found found = this.target.find(code);
		if (found == null)
			return;

		MethodTypeDesc lambdaDesc = MethodTypeDesc.of(found.output(), found.inputs());
		ClassDesc[] lambdaParams = lambdaDesc.parameterArray();
		int lambdaFlags = AccessFlags.ofMethod(AccessFlag.PRIVATE, AccessFlag.STATIC, AccessFlag.SYNTHETIC).flagsMask();
		boolean targetIsInterface = context.clazz().model().flags().flags().contains(AccessFlag.INTERFACE);
		DirectMethodHandleDesc.Kind kind = targetIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;

		for (Selection selection : found.selections()) {
			String lambdaName = "wrap_op_" + context.transformerId().hashCode();
			selection.extract(
					lambdaName, lambdaDesc, lambdaFlags, method -> {},
					head -> {
						// invoke WrapOpValidation
						head.aload(0); // load param array
						head.ldc(lambdaParams.length);
						head.invokestatic(VALIDATION_DESC, CHECK_COUNT, CHECK_COUNT_DESC);

						// unpack array
						for (int i = 0; i < lambdaParams.length; i++) {
							ClassDesc param = lambdaParams[i];
							head.aload(0); // push array
							head.constantInstruction(i); // push index
							head.aaload(); // read from array
							head.checkcast(param); // validate type
						}
					},
					tail -> tail.returnInstruction(TypeKind.from(lambdaDesc.returnType())),
					replacement -> {
						DirectMethodHandleDesc lambdaHandle = MethodHandleDesc.ofMethod(kind, context.clazz().desc(), lambdaName, lambdaDesc);

						// push the Operation to the stack
						replacement.invokedynamic(DynamicCallSiteDesc.of(
								LMF, // bootstrap
								"call", // interface method name
								OPERATION_LAMBDA_FACTORY, // lambda factory
								// args - see LMF javadoc for info
								OPERATION_TYPE, lambdaHandle, OPERATION_TYPE
						));

						// invoke the hook
						replacement.invokestatic(this.hook.owner(), this.hook.methodName(), this.hook.invocationType());
					}
			);
		}
	}

	@Override
	public String describe() {
		return "Wrap operation @ [" + this.target.describe() + "] in [" + this.method + "] calling [" + this.hook + ']';
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
