package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.MethodTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import org.glavo.classfile.AccessFlag;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

/**
 * A transformer that injects hook callbacks into target methods.
 */
public abstract class HookingTransformer extends CodeTargetingTransformer {
	protected final Hook hook;

	protected HookingTransformer(ClassTarget classes, MethodTarget method, Slice slice, Hook hook) {
		super(classes, method, slice);
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

	@FunctionalInterface
	public interface HookProvider {
		/**
		 * Create a descriptor for a hook method based on {@link #hook}.
		 * @param returnType the expected return type of the hook
		 * @param implicitParameters list of parameter types required by this transform, added to the front of the parameter list
		 */
		DirectMethodHandleDesc get(ClassDesc returnType, List<ClassDesc> implicitParameters);
	}

	public record Hook(Owner owner, String name, List<ContextParameter> params) {
		public static final DualCodec<Hook> CODEC = CompositeCodec.of(
				Owner.CODEC.fieldOf("class"), Hook::owner,
				Codec.STRING.fieldOf("name"), Hook::name,
				ContextParameter.CODEC.listOf().optional(List.of()).fieldOf("parameters"), Hook::params,
				Hook::new
		);

		public Hook(Owner owner, String name) {
			this(owner, name, List.of());
		}

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
			).codec();

			public static final Codec<Owner> CODEC = fullCodec.withAlternative(nameOnlyCodec);

			public Owner(ClassDesc type) {
				this(type, false);
			}
		}
	}
}
