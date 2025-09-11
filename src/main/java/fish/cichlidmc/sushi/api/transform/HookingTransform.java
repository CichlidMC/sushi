package fish.cichlidmc.sushi.api.transform;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.validation.MethodInfo;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class HookingTransform extends CodeTargetingTransform {
	protected final Hook hook;

	protected HookingTransform(MethodTarget method, Hook hook) {
		super(method);
		this.hook = hook;
	}

	@Override
	protected final void apply(TransformContext context, TransformableCode code) throws TransformException {
		this.apply(context, code, (returnType, implicitParameters) -> {
			DirectMethodHandleDesc desc = this.hook.createDesc(returnType, implicitParameters);
			context.validation().ifPresent(validation -> this.validateHook(
					validation.findMethod(desc).orElseThrow(() -> new TransformException("Hook method is missing: " + desc))
			));
			return desc;
		});
	}

	protected abstract void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException;

	protected void validateHook(MethodInfo method) throws TransformException {
		Set<AccessFlag> flags = method.flags();
		if (!flags.contains(AccessFlag.STATIC) || !flags.contains(AccessFlag.PUBLIC)) {
			throw new TransformException("Hook method must be public and static: " + this.hook);
		}
	}

	public interface HookProvider {
		/**
		 * Create a descriptor for a hook method based on {@link #hook}.
		 * @param returnType the expected return type of the hook
		 * @param implicitParameters list of parameter types required by this transform, added to the front of the parameter list
		 */
		DirectMethodHandleDesc get(ClassDesc returnType, List<ClassDesc> implicitParameters);
	}

	public record Hook(Owner owner, String name, List<ContextParameter> params) {
		public static final Codec<Hook> CODEC = CompositeCodec.of(
				Owner.CODEC.fieldOf("class"), Hook::owner,
				Codec.STRING.fieldOf("name"), Hook::name,
				ContextParameter.CODEC.listOf().optional(List.of()).fieldOf("parameters"), Hook::params,
				Hook::new
		).asCodec();

		private DirectMethodHandleDesc createDesc(ClassDesc returnType, List<ClassDesc> baseParams) {
			List<ClassDesc> params = new ArrayList<>(baseParams);
			this.params.forEach(param -> params.add(param.type()));
			MethodTypeDesc desc = MethodTypeDesc.of(returnType, params);
			return MethodHandleDesc.ofMethod(this.invokeKind(), this.owner.type, this.name, desc);
		}

		private DirectMethodHandleDesc.Kind invokeKind() {
			return this.owner.isInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
		}

		public record Owner(ClassDesc type, boolean isInterface) {
			private static final Codec<Owner> nameOnlyCodec = ClassDescs.CLASS_CODEC.xmap(Owner::new, Owner::type);
			private static final Codec<Owner> fullCodec = CompositeCodec.of(
					ClassDescs.CLASS_CODEC.fieldOf("name"), Owner::type,
					Codec.BOOL.optional(false).fieldOf("interface"), Owner::isInterface,
					Owner::new
			).asCodec();

			public static final Codec<Owner> CODEC = fullCodec.withAlternative(nameOnlyCodec);

			public Owner(ClassDesc type) {
				this(type, false);
			}
		}
	}
}
