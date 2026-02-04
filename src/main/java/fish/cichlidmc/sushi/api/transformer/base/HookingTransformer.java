package fish.cichlidmc.sushi.api.transformer.base;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodTarget;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.InheritanceRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.infra.Slice;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.transformer.CoercionsImpl;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/// A transformer that injects hook callbacks into target methods.
public abstract class HookingTransformer extends CodeTargetingTransformer {
	protected final Hook hook;

	protected HookingTransformer(ClassPredicate predicate, MethodTarget method, Slice slice, Hook hook) {
		super(predicate, method, slice);
		this.hook = hook;
	}

	@Override
	protected final void apply(TransformContext context, TransformableCode code) throws TransformException {
		if (!this.hook.coercions.isEmpty()) {
			this.hook.coercions.forEach((from, into) -> context.require(new ClassRequirement(
					"Cannot coerce from a type that doesn't exist", from,
					new InheritanceRequirement("Cannot coerce into a type that doesn't exist", into)
			)));
		}

		this.apply(context, code, (returnType, baseParameters) -> {
			DirectMethodHandleDesc desc = this.hook.createHandle(returnType, baseParameters);

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
		/// Create a descriptor for a hook method based on [#hook].
		/// @param returnType the expected return type of the hook
		/// @param baseParameters list of parameter types required by this transform, not including [ContextParameter]s
		/// @return a handle to the hook method. Note that it may have additional parameters due to [ContextParameter]s.
		DirectMethodHandleDesc get(ClassDesc returnType, List<ClassDesc> baseParameters);
	}

	/// Describes a callback method that will be invoked by an injected method call.
	/// @param owner a description of the class containing the hook method
	/// @param params a list of [ContextParameter]s, providing additional context and functionality
	public record Hook(Owner owner, String name, Coercions coercions, List<ContextParameter> params) {
		public static final DualCodec<Hook> CODEC = CompositeCodec.of(
				Owner.CODEC.fieldOf("class"), Hook::owner,
				Codec.STRING.fieldOf("name"), Hook::name,
				Coercions.CODEC.asCodec().optional(Coercions.EMPTY).fieldOf("coercions"), Hook::coercions,
				ContextParameter.CODEC.listOf().optional(List.of()).fieldOf("parameters"), Hook::params,
				Hook::new
		);

		/// Codec that forbids [ContextParameter]s.
		public static final DualCodec<Hook> PARAMETERLESS_CODEC = CODEC.validate(hook -> {
			if (hook.params.isEmpty()) {
				return Result.success(hook);
			} else {
				return Result.error("Hook must not have context parameters");
			}
		});

		public Hook(Owner owner, String name) {
			this(owner, name, List.of());
		}

		public Hook(Owner owner, String name, List<ContextParameter> params) {
			this(owner, name, Coercions.EMPTY, params);
		}

		/// Create a handle descriptor for this hook method.
		/// @param returnType the expected return type of the hook
		/// @param baseParams a list of prefix parameters, which context parameters will appear after
		private DirectMethodHandleDesc createHandle(ClassDesc returnType, List<ClassDesc> baseParams) {
			List<ClassDesc> params = new ArrayList<>(baseParams);

			// swap out each base parameter for a coerced type if present
			for (int i = 0; i < params.size(); i++) {
				ClassDesc type = params.get(i);
				Optional<ClassDesc> replacement = this.coercions.get(type);
				if (replacement.isPresent()) {
					params.set(i, replacement.get());
				}
			}

			this.params.forEach(param -> params.add(param.type()));
			MethodTypeDesc desc = MethodTypeDesc.of(this.coercions.coerce(returnType), params);
			return MethodHandleDesc.ofMethod(this.invokeKind(), this.owner.type, this.name, desc);
		}

		private DirectMethodHandleDesc.Kind invokeKind() {
			return this.owner.isInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
		}

		/// Describes the class containing a hook method.
		/// @param type the class's descriptor
		/// @param isInterface true if the hook is in an interface
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

		/// Defines a mapping between original types and "coerced" types that will replace
		/// parameter and return types of hooks. This allows hooks to interact with code
		/// that references inaccessible classes by only referring to them as a weaker, accessible type.
		public sealed interface Coercions permits CoercionsImpl {
			MapCodec<Coercions> CODEC = MapCodec.map(ClassDescs.CLASS_OR_ARRAY_CODEC, ClassDescs.CLASS_OR_ARRAY_CODEC).xmap(
					CoercionsImpl::ofTrusted, coercions -> ((CoercionsImpl) coercions).map()
			);

			Coercions EMPTY = of(Map.of());

			/// @return the coerced type, if present
			Optional<ClassDesc> get(ClassDesc type);

			/// @return the coerced type if present, otherwise the original type
			ClassDesc coerce(ClassDesc type);

			/// @return true if there are no types to coerce
			boolean isEmpty();

			/// Invoke the given consumer on each type coercion.
			///
			/// The first argument is the original type, and the second is the new coerced one.
			void forEach(BiConsumer<ClassDesc, ClassDesc> consumer);

			/// Create a new set of coercions based on the given map between original and coerced types.
			static Coercions of(Map<ClassDesc, ClassDesc> map) {
				return CoercionsImpl.of(map);
			}
		}
	}
}
