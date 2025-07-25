package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.util.method.MethodDescription;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.sushi.api.validation.MethodInfo;
import fish.cichlidmc.tinycodecs.Codec;

import java.lang.constant.DirectMethodHandleDesc;
import java.lang.reflect.AccessFlag;
import java.util.Set;

public abstract class HookingTransform extends CodeTargetingTransform {
	public static final Codec<DirectMethodHandleDesc> HOOK_CODEC = MethodDescription.CODEC.comapFlatMap(
			description -> description.toMethodHandleDesc(DirectMethodHandleDesc.Kind.STATIC),
			MethodDescription::ofMethodHandleDesc
	);

	protected final DirectMethodHandleDesc hook;

	protected HookingTransform(MethodTarget method, DirectMethodHandleDesc hook) {
		super(method);
		this.hook = hook;
	}

	@Override
	protected final void apply(TransformContext context, TransformableCode code) throws TransformException {
		context.validation().ifPresent(validation -> this.extraHookValidation(
				validation.findMethod(this.hook).orElseThrow(() -> new TransformException("Hook method is missing"))
		));

		this.doApply(context, code);
	}

	protected abstract void doApply(TransformContext context, TransformableCode code) throws TransformException;

	protected void extraHookValidation(MethodInfo method) throws TransformException {
		Set<AccessFlag> flags = method.flags();
		if (!flags.contains(AccessFlag.STATIC) || !flags.contains(AccessFlag.PUBLIC)) {
			throw new TransformException("Hook method must be public and static: " + this.hook);
		}
	}
}
