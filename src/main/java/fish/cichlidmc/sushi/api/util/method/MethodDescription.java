package fish.cichlidmc.sushi.api.util.method;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.instruction.InvokeInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Partially or fully describes one or more methods. Always specifies at least a method name. May additionally specify:
 * <ul>
 *     <li>Name of containing class</li>
 *     <li>Parameter types</li>
 *     <li>Return type</li>
 * </ul>
 */
public record MethodDescription(String name, Optional<ClassDesc> containingClass, Optional<List<ClassDesc>> parameters, Optional<ClassDesc> returnType) {
	// Parses from a string, either "myMethod" or "com.example.MyClass.myMethod"
	public static final Codec<MethodDescription> STRING_CODEC = Codec.STRING.comapFlatMap(MethodDescription::parse, method -> method.name);

	public static final MapCodec<MethodDescription> FULL_CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), method -> method.name,
			ClassDescs.CLASS_CODEC.optional().fieldOf("class"), method -> method.containingClass,
			ClassDescs.ANY_CODEC.listOf().optional().fieldOf("parameters"), method -> method.parameters,
			ClassDescs.ANY_CODEC.optional().fieldOf("return"), method -> method.returnType,
			MethodDescription::new
	);

	public static final Codec<MethodDescription> CODEC = FULL_CODEC.asCodec().withAlternative(STRING_CODEC);

	public boolean matches(InvokeInstruction invoke) {
		if (!this.name.equals(invoke.name().stringValue()))
			return false;

		if (this.containingClass.isPresent()) {
			if (!this.containingClass.get().equals(invoke.owner().asSymbol())) {
				return false;
			}
		}

		return this.parametersMatch(() -> invoke.typeSymbol().parameterArray())
				&& this.returnTypeMatches(() -> invoke.typeSymbol().returnType());
	}

	public boolean matches(TransformableMethod method) {
		if (!this.name.equals(method.model().methodName().stringValue()))
			return false;

		if (this.containingClass.isPresent()) {
			if (!this.containingClass.get().equals(method.owner().desc())) {
				return false;
			}
		}

		return this.parametersMatch(method::parameterTypes) && this.returnTypeMatches(method::returnType);
	}

	private boolean parametersMatch(Supplier<ClassDesc[]> actualGetter) {
		if (this.parameters.isEmpty())
			return true;

		List<ClassDesc> expected = this.parameters.get();
		ClassDesc[] actual = actualGetter.get();

		if (expected.size() != actual.length)
			return false;

		for (int i = 0; i < actual.length; i++) {
			if (!expected.get(i).equals(actual[i])) {
				return false;
			}
		}

		return true;
	}

	private boolean returnTypeMatches(Supplier<ClassDesc> returnType) {
		return this.returnType.map(type -> type.equals(returnType.get())).orElse(true);
	}

	public CodecResult<DirectMethodHandleDesc> toMethodHandleDesc(DirectMethodHandleDesc.Kind kind) {
		return hasClass(this).flatMap(MethodDescription::hasParamsAndReturn).map(self -> MethodHandleDesc.ofMethod(
				kind, self.containingClass.orElseThrow(), self.name,
				MethodTypeDesc.of(self.returnType.orElseThrow(), self.parameters.orElseThrow())
		));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.returnType.ifPresent(type -> builder.append(ClassDescs.fullName(type)).append(' '));
		this.containingClass.ifPresent(clazz -> builder.append(ClassDescs.fullName(clazz)).append('.'));
		builder.append(this.name);
		this.parameters.ifPresent(params -> {
			builder.append('(');
			for (int i = 0; i < params.size(); i++) {
				ClassDesc param = params.get(i);
				builder.append(ClassDescs.fullName(param));
				if (i + 1 < params.size()) {
					builder.append(", ");
				}
			}
			builder.append(')');
		});
		return builder.toString();
	}

	public static MethodDescription ofMethodHandleDesc(DirectMethodHandleDesc desc) {
		return new MethodDescription(
				desc.methodName(),
				Optional.of(desc.owner()),
				Optional.of(desc.invocationType().parameterList()),
				Optional.of(desc.invocationType().returnType())
		);
	}

	private static CodecResult<MethodDescription> parse(String string) {
		int lastDot = string.lastIndexOf('.');
		if (lastDot == string.length() - 1) {
			return CodecResult.error("Invalid method: should be formatted like 'com.example.MyClass.myMethod', not " + string);
		}

		if (lastDot == -1) {
			MethodDescription description = new MethodDescription(string, Optional.empty(), Optional.empty(), Optional.empty());
			return CodecResult.success(description);
		}

		String className = string.substring(0, lastDot);
		try {
			ClassDesc desc = ClassDesc.of(className);
			if (!desc.isClassOrInterface()) {
				return CodecResult.error("Not a class or interface: " + className);
			}

			String name = string.substring(lastDot + 1);
			MethodDescription method = new MethodDescription(name, Optional.of(desc), Optional.empty(), Optional.empty());
			return CodecResult.success(method);
		} catch (IllegalArgumentException e) {
			return CodecResult.error("Malformed class name: " + e.getMessage());
		}
	}

	private static CodecResult<MethodDescription> hasClass(MethodDescription description) {
		if (description.containingClass.isPresent()) {
			return CodecResult.success(description);
		} else {
			return CodecResult.error("Class not specified: " + description);
		}
	}

	private static CodecResult<MethodDescription> hasParamsAndReturn(MethodDescription description) {
		if (description.parameters.isEmpty()) {
			return CodecResult.error("Parameters are not specified: " + description);
		} else if (description.returnType.isEmpty()) {
			return CodecResult.error("Return type is not specified: " + description);
		} else {
			return CodecResult.success(description);
		}
	}
}
