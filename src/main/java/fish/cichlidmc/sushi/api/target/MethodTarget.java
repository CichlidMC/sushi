package fish.cichlidmc.sushi.api.target;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.instruction.InvokeInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Fuzzily selects methods for transforms to target.
 * <p>
 * Always specifies a name, but may also specify the owner class and descriptor for disambiguation.
 * <p>
 * The expected number of matches may also be set. By default, a MethodTarget is expected to match exactly once.
 * If the actual number of matches doesn't equal the expected number, a {@link TransformException} will be thrown.
 * @param expected the expected number of matches. Non-negative; a value of 0 indicates unlimited matches.
 */
public record MethodTarget(String name, Optional<ClassDesc> owner, Desc desc, int expected) {
	public static final int EXPECTED_UNLIMITED = 0;
	public static final int DEFAULT_EXPECTED = 1;

	private static final Codec<MethodTarget> nameOnlyCodec = Codec.STRING.xmap(MethodTarget::new, MethodTarget::name);
	private static final Codec<MethodTarget> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), MethodTarget::name,
			ClassDescs.CLASS_CODEC.optional().fieldOf("class"), MethodTarget::owner,
			Desc.CODEC.optional(Desc.EMPTY).fieldOf("descriptor"), MethodTarget::desc,
			SushiCodecs.NON_NEGATIVE_INT.optional(DEFAULT_EXPECTED).fieldOf("expected"), MethodTarget::expected,
			MethodTarget::new
	).asCodec();

	public static final Codec<MethodTarget> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public MethodTarget(String name) {
		this(name, Optional.empty(), Desc.EMPTY, DEFAULT_EXPECTED);
	}

	public List<TransformableMethod> find(TransformableClass clazz) throws TransformException {
		List<TransformableMethod> found = clazz.methods().stream().filter(this::matches).collect(Collectors.toList());
		this.checkExpected(found);
		return found;
	}

	public List<InvokeInstruction> find(InstructionList instructions) throws TransformException {
		List<InvokeInstruction> list = instructions.asList().stream()
				.filter(instruction -> instruction instanceof InvokeInstruction invoke && this.matches(invoke))
				.map(invoke -> (InvokeInstruction) invoke)
				.toList();

		this.checkExpected(list);
		return list;
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
			throw TransformException.of("MethodTarget did not match the expected number of times", e -> {
				e.addDetail("Expected Matches", this.expected == 0 ? "<unlimited>" : this.expected);
				e.addDetail("Actual Matches", matches.size());
				for (Object match : matches) {
					e.addDetail("Match", match);
				}
			});
		}
	}

	public record Desc(Optional<List<ClassDesc>> params, Optional<ClassDesc> returnType) {
		public static final Codec<Desc> CODEC = CompositeCodec.of(
				ClassDescs.ANY_CODEC.listOf().optional().fieldOf("params"), Desc::params,
				ClassDescs.ANY_CODEC.optional().fieldOf("return"), Desc::returnType,
				Desc::of
		).asCodec();

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
