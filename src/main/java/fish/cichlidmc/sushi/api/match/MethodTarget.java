package fish.cichlidmc.sushi.api.match;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;

import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/// Fuzzily selects methods for transforms to target.
///
/// Always specifies a name, but may also specify the owner class and descriptor for disambiguation.
///
/// The expected number of matches may also be set. By default, a MethodTarget is expected to match exactly once.
/// If the actual number of matches doesn't equal the expected number, a [TransformException] will be thrown.
/// @param expected the expected number of matches. Non-negative; a value of 0 indicates unlimited matches.
public record MethodTarget(String name, Optional<ClassDesc> owner, Desc desc, int expected) {
	public static final int EXPECTED_UNLIMITED = 0;
	public static final int DEFAULT_EXPECTED = 1;

	private static final Codec<MethodTarget> nameOnlyCodec = Codec.STRING.xmap(MethodTarget::new, MethodTarget::name);
	private static final Codec<MethodTarget> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), MethodTarget::name,
			ClassDescs.CLASS_CODEC.optional().fieldOf("class"), MethodTarget::owner,
			Desc.CODEC.codec().optional(Desc.EMPTY).fieldOf("descriptor"), MethodTarget::desc,
			SushiCodecs.NON_NEGATIVE_INT.optional(DEFAULT_EXPECTED).fieldOf("expected"), MethodTarget::expected,
			MethodTarget::new
	).codec();

	public static final Codec<MethodTarget> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public MethodTarget(String name) {
		this(name, Optional.empty(), Desc.EMPTY, DEFAULT_EXPECTED);
	}

	public MethodTarget(String name, int expected) {
		this(name, Optional.empty(), Desc.EMPTY, expected);
	}

	public MethodTarget(String name, ClassDesc owner) {
		this(name, Optional.of(owner), Desc.EMPTY, DEFAULT_EXPECTED);
	}

	/// @return a list of methods matching this target
	/// @throws TransformException if the number of matches does not equal the [#expected] number
	public List<TransformableMethod> find(TransformableClass clazz) throws TransformException {
		List<TransformableMethod> found = clazz.methods().stream().filter(this::matches).collect(Collectors.toList());
		this.checkExpected(found);
		return found;
	}

	/// Search the given set of instructions for method invocations matching this target.
	/// @return a set of matched instructions
	/// @throws TransformException if the number of matches does not equal the [#expected] number
	public NavigableSet<InstructionHolder.Real<InvokeInstruction>> find(NavigableSet<InstructionHolder<?>> instructions) throws TransformException {
		NavigableSet<InstructionHolder.Real<InvokeInstruction>> found = new TreeSet<>();

		for (InstructionHolder<?> instruction : instructions) {
			if (instruction.get() instanceof InvokeInstruction invoke && this.matches(invoke)) {
				found.add(instruction.checkHoldingReal(InvokeInstruction.class));
			}
		}

		this.checkExpected(found);
		return found;
	}

	public boolean matches(InvokeInstruction invoke) {
		if (!invoke.name().equalsString(this.name))
			return false;

		if (!this.desc.matches(invoke.typeSymbol()))
			return false;

		return this.owner.isEmpty() || this.owner.get().equals(invoke.owner().asSymbol());
	}

	public boolean matches(TransformableMethod method) {
		MethodModel model = method.model();
		if (!model.methodName().equalsString(this.name))
			return false;

		if (!this.desc.matches(model.methodTypeSymbol()))
			return false;

		return this.owner.isEmpty() || this.owner.get().equals(method.owner().desc());
	}

	private void checkExpected(Collection<?> matches) throws TransformException {
		if (matches.isEmpty() || (this.expected != EXPECTED_UNLIMITED && this.expected != matches.size())) {
			throw new TransformException("MethodTarget did not match the expected number of times", details -> {
				details.add("Expected Matches", this.expected == 0 ? "<unlimited>" : this.expected);
				details.add("Actual Matches", matches.size());
				for (Object match : matches) {
					details.add("Match", match);
				}
			});
		}
	}

	public record Desc(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) {
		public static final DualCodec<Desc> CODEC = CompositeCodec.of(
				ClassDescs.ANY_CODEC.listOf().optional().fieldOf("params"), Desc::params,
				ClassDescs.ANY_CODEC.optional().fieldOf("return"), Desc::returnType,
				Desc::of
		);

		public static final Desc EMPTY = new Desc(Optional.empty(), Optional.empty());

		public boolean matches(MethodTypeDesc desc) {
			if (this.params.isPresent() && !desc.parameterList().equals(this.params.get()))
				return false;

			return this.returnType.isEmpty() || desc.returnType().equals(this.returnType.get());
		}

		public static Desc of(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) {
			return params.isEmpty() && returnType.isEmpty() ? EMPTY : new Desc(params, returnType);
		}
	}
}
