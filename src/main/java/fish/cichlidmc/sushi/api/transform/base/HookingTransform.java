package fish.cichlidmc.sushi.api.transform.base;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.glavo.classfile.AccessFlag;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

/**
 * A transform that injects hook callbacks into target methods.
 */
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

			context.require(new ClassRequirement(
					"Class containing hook must exist", desc.owner(),
					new MethodRequirement(
							"Expected hook method matching target",
							desc.methodName(), desc.invocationType(),
							FlagsRequirement.builder("Hook methods must be public and static")
									.require(AccessFlag.PUBLIC)
									.require(AccessFlag.STATIC)
									.build()
					)
			));

			return desc;
		});
	}

	protected abstract void apply(TransformContext context, TransformableCode code, HookProvider provider) throws TransformException;

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
