package fish.cichlidmc.sushi.api.match.method;

import fish.cichlidmc.fishflakes.api.Result;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/// Fuzzily selects methods to be targeted by transforms.
///
/// Always specifies a name, but may also specify the owner class and descriptor for disambiguation.
public record MethodSelector(String name, Optional<ClassDesc> owner, Optional<Desc> desc) {
	private static final Codec<MethodSelector> nameOnlyCodec = Codec.STRING.xmap(MethodSelector::new, MethodSelector::name);
	private static final Codec<MethodSelector> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), MethodSelector::name,
			ClassDescs.CLASS_CODEC.optional().fieldOf("class"), MethodSelector::owner,
			Desc.CODEC.codec().optional().fieldOf("descriptor"), MethodSelector::desc,
			MethodSelector::new
	).codec();

	public static final Codec<MethodSelector> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public MethodSelector(String name) {
		this(name, Optional.empty(), Optional.empty());
	}

	public MethodSelector(String name, ClassDesc owner) {
		this(name, Optional.of(owner), Optional.empty());
	}

	public MethodSelector(String name, Desc desc) {
		this(name, Optional.empty(), Optional.of(desc));
	}

	/// @return a list of methods matching this target
	public List<TransformableMethod> find(TransformableClass clazz) {
		return clazz.methods().values().stream().filter(this::matches).collect(Collectors.toList());
	}

	/// Search the given set of instructions for method invocations matching this target.
	/// @return a set of matched instructions, ordered by occurrence in the original set
	public NavigableSet<InstructionHolder.Real<InvokeInstruction>> find(NavigableSet<InstructionHolder<?>> instructions) {
		NavigableSet<InstructionHolder.Real<InvokeInstruction>> found = new TreeSet<>();

		for (InstructionHolder<?> instruction : instructions) {
			if (instruction.get() instanceof InvokeInstruction invoke && this.matches(invoke)) {
				found.add(instruction.checkHoldingReal(InvokeInstruction.class));
			}
		}

		return found;
	}

	/// @return true if this selector matches the method invoked by the given instruction
	public boolean matches(InvokeInstruction invoke) {
		if (!invoke.name().equalsString(this.name))
			return false;

		if (this.desc.isPresent() && !this.desc.get().matches(invoke.typeSymbol()))
			return false;

		return this.owner.isEmpty() || this.owner.get().equals(invoke.owner().asSymbol());
	}

	/// @return true if this selector matches the given method
	public boolean matches(TransformableMethod method) {
		MethodModel model = method.model();
		if (!model.methodName().equalsString(this.name))
			return false;

		if (this.desc.isPresent() && !this.desc.get().matches(model.methodTypeSymbol()))
			return false;

		return this.owner.isEmpty() || this.owner.get().equals(method.owner().desc());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("MethodSelector[name=");
		builder.append(this.name);

		this.owner.ifPresent(owner -> {
			builder.append(", owner=");
			builder.append(ClassDescs.fullName(owner));
		});

		this.desc.ifPresent(desc -> {
			builder.append(", desc=");
			builder.append(desc);
		});

		return builder.append(']').toString();
	}

	/// Describes the descriptor of a method. May specify parameter types, the return type, or both.
	public static final class Desc {
		@SuppressWarnings("Convert2MethodRef") // generics freak out
		public static final DualCodec<Desc> CODEC = CompositeCodec.of(
				ClassDescs.ANY_CODEC.listOf().optional().fieldOf("params"), desc -> desc.orElseThrow().params,
				ClassDescs.ANY_CODEC.optional().fieldOf("return"), desc -> desc.orElseThrow().returnType,
				(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) -> of(params, returnType)
		).comapFlatMap(
				optional -> {
					if (optional.isPresent()) {
						return Result.success(optional.get());
					} else {
						return Result.error("Desc must specify either params or return type");
					}
				},
				Optional::of
		);

		public final Optional<List<ClassDesc>> params;
		public final Optional<ClassDesc> returnType;

		private Desc(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) {
			if (params.isEmpty() && returnType.isEmpty()) {
				throw new IllegalArgumentException("Cannot create an empty Desc");
			}

			this.params = params;
			this.returnType = returnType;
		}

		/// @return true if this Desc describes the given descriptor.
		public boolean matches(MethodTypeDesc desc) {
			if (this.params.isPresent() && !desc.parameterList().equals(this.params.get()))
				return false;

			return this.returnType.isEmpty() || desc.returnType().equals(this.returnType.get());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Desc that))
				return false;

			return this.params.equals(that.params) && this.returnType.equals(that.returnType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.params, this.returnType);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("Desc[");

			this.params.ifPresent(params -> builder.append("params=").append(params));
			this.returnType.ifPresent(returnType -> {
				if (this.params.isPresent())
					builder.append(", ");

				builder.append("return=").append(returnType);
			});

			return builder.append(']').toString();
		}

		/// Create a new Desc that specifies both a parameter type list and a return type.
		public static Desc of(List<ClassDesc> params, ClassDesc returnType) {
			return new Desc(Optional.of(params), Optional.of(returnType));
		}

		/// Create a new Desc that only specifies parameters.
		public static Desc of(List<ClassDesc> params) {
			return new Desc(Optional.of(params), Optional.empty());
		}

		/// Create a new Desc that only specifies a return type.
		public static Desc of(ClassDesc returnType) {
			return new Desc(Optional.empty(), Optional.of(returnType));
		}

		/// Create a new Desc if either the given params or return type are present. Otherwise, returns empty.
		public static Optional<Desc> of(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) {
			if (params.isEmpty() && returnType.isEmpty())
				return Optional.empty();

			return Optional.of(new Desc(params, returnType));
		}
	}
}
