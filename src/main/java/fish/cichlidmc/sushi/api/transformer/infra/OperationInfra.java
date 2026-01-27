package fish.cichlidmc.sushi.api.transformer.infra;

import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/// Infrastructure for transformers using Operations.
public final class OperationInfra {
	/// The [ClassDesc] of [Operation].
	public static final ClassDesc OPERATION_DESC = ClassDescs.of(Operation.class);
	/// The [ClassDesc] of this class.
	public static final ClassDesc INFRA_DESC = ClassDescs.of(OperationInfra.class);

	// these two fields are not stored in a single handle since the
	// constructor would add an implicit self reference to the desc

	/// The name of [#call(Object...)].
	public static final String CALL_NAME = "call";
	/// The [descriptor][MethodTypeDesc] of [#call(java.lang.Object...).
	public static final MethodTypeDesc CALL_DESC = MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object.arrayType());

	/// Handle for [#checkCount(java.lang.Object[], int)].
	/// Invoked at runtime to check validity of arguments passed from hooks.
	public static final DirectMethodHandleDesc CHECK_COUNT_HANDLE = MethodHandleDesc.ofMethod(
			DirectMethodHandleDesc.Kind.STATIC,
			INFRA_DESC, "checkCount",
			MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_int)
	);

	/// Handle for the standard [lambda factory][LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType , MethodType, MethodHandle , MethodType)].
	public static final DirectMethodHandleDesc LMF = ConstantDescs.ofCallsiteBootstrap(
			ClassDescs.of(LambdaMetafactory.class), "metafactory", ConstantDescs.CD_CallSite,
			// args
			ConstantDescs.CD_MethodType, ConstantDescs.CD_MethodHandle, ConstantDescs.CD_MethodType
	);

	/// The default Invocation Type when generating an Operation lambda object.
	/// - Parameters: types of captured locals (none)
	/// - Return type: Type of generated lambda object ([Operation])
	public static final MethodTypeDesc DEFAULT_INVOCATION_TYPE = MethodTypeDesc.of(OPERATION_DESC);

	private OperationInfra() {}

	/// Checks that the given `Object[]` is of the expected size.
	/// Intended to be invoked at runtime by generated code.
	@SuppressWarnings("unused")
	public static void checkCount(Object[] args, int expectedSize) {
		if (args.length != expectedSize) {
			throw new RuntimeException(
					"Received invalid argument array passed to Operation: expected " + expectedSize + " element(s), got " + args.length
			);
		}
	}
}
