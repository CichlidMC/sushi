package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.CodecResult;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
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

	public Collection<MethodNode> findOrThrow(ClassNode clazz) throws TransformException {
		Predicate<MethodNode> test = method -> this.description.matches(clazz, method);
		List<MethodNode> found = clazz.methods.stream().filter(test).collect(Collectors.toList());
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
		if (this.description.parameters.isPresent()) {
			return CodecResult.error("Cannot encode a target with parameters to a simple string");
		} else if (this.description.returnType.isPresent()) {
			return CodecResult.error("Cannot encode a target with a return type to a simple string");
		} else if (this.expect != -1 && this.expect != 1) {
			return CodecResult.error("Cannot encode a target with a custom expect amount to a simple string");
		}

		StringBuilder builder = new StringBuilder();
		this.description.containingClass.ifPresent(clazz -> builder.append(clazz).append('.'));
		builder.append(this.description.name);

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
