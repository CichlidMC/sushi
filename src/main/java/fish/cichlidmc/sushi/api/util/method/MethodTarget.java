package fish.cichlidmc.sushi.api.util.method;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.instruction.InvokeInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A method or set of methods to be targeted by transforms. May specify the expected number of methods that match it.
 */
public final class MethodTarget {
	// simple format: name, maybe class name, wildcard allowed, default expects 1 unless wildcard
	public static final Codec<MethodTarget> SIMPLE_CODEC = Codec.STRING.flatXmap(MethodTarget::parse, MethodTarget::toSimpleString);

	public static final Codec<MethodTarget> FULL_CODEC = CompositeCodec.of(
			MethodDescription.FULL_CODEC, target -> target.description,
			Codec.INT.optional(1).fieldOf("expect"), target -> target.expect,
			MethodTarget::new
	).asCodec();

	public static final Codec<MethodTarget> CODEC = FULL_CODEC.withAlternative(SIMPLE_CODEC);

	public final MethodDescription description;
	public final int expect;

	private MethodTarget(MethodDescription description, int expect) {
		this.description = description;
		this.expect = expect;
	}

	public List<TransformableMethod> findOrThrow(TransformableClass clazz) throws TransformException {
		List<TransformableMethod> found = clazz.methods().stream().filter(this.description::matches).collect(Collectors.toList());
		if (this.expect != -1 && found.size() != this.expect) {
			throw new TransformException("Method target expected to match " + this.expect + " time(s), but matched " + found.size() + " time(s).");
		}
		return found;
	}

	public List<InvokeInstruction> findOrThrow(InstructionList instructions, boolean single) throws TransformException {
		List<InvokeInstruction> found = new ArrayList<>();
		for (CodeElement element : instructions.asList()) {
			if (!(element instanceof InvokeInstruction invoke))
				continue;

			if (!this.description.matches(invoke))
				continue;

			if (single && !found.isEmpty()) {
				InvokeInstruction existing = found.getFirst();
				// no need to check name, must match already
				if (!existing.typeSymbol().equals(invoke.typeSymbol())) {
					throw new TransformException(String.format(
							"MethodTarget matched multiple conflicting methods: [%s] and [%s]",
							invoke.typeSymbol(), existing.typeSymbol()
					));
				}

				if (existing.opcode() != invoke.opcode()) {
					throw new TransformException(String.format(
							"MethodTarget matched multiple conflicting invoke instructions: [%s] and [%s]",
							invoke.opcode(), existing.opcode()
					));
				}
			}

			found.add(invoke);
		}

		if (this.expect != -1 && found.size() != this.expect) {
			throw new TransformException("Method target expected to match " + this.expect + " time(s), but matched " + found.size() + " time(s).");
		}

		return found;
	}

	@Override
	public String toString() {
		return this.expect == 1
				? this.description.toString()
				: this.description.toString() + " (expected: " + this.expect + ')';
	}

	private CodecResult<String> toSimpleString() {
		if (this.description.parameters().isPresent()) {
			return CodecResult.error("Cannot encode a target with parameters to a simple string");
		} else if (this.description.returnType().isPresent()) {
			return CodecResult.error("Cannot encode a target with a return type to a simple string");
		} else if (this.expect != -1 && this.expect != 1) {
			return CodecResult.error("Cannot encode a target with a custom expect amount to a simple string");
		}

		StringBuilder builder = new StringBuilder();
		this.description.containingClass().ifPresent(clazz -> builder.append(clazz).append('.'));
		builder.append(this.description.name());

		if (this.expect == -1) {
			builder.append('*');
		}

		return CodecResult.success(builder.toString());
	}

	private static CodecResult<MethodTarget> parse(String name) {
		boolean wildcard = isWildcard(name);
		int expect = wildcard ? -1 : 1;
		String actualName = wildcard ? name.substring(0, name.length() - 1) : name;
		CodecResult<MethodDescription> description = MethodDescription.STRING_CODEC.decode(new JsonString(actualName));
		return description.map(desc -> new MethodTarget(desc, expect));
	}

	private static boolean isWildcard(String name) {
		return name.endsWith("*");
	}
}
