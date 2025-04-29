package fish.cichlidmc.sushi.api.util.method;

import fish.cichlidmc.sushi.api.util.JavaType;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import fish.cichlidmc.tinycodecs.util.Either;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
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
public final class MethodDescription {
	// Parses from a string, either "myMethod" or "com.example.MyClass.myMethod"
	public static final Codec<MethodDescription> STRING_CODEC = Codec.STRING.comapFlatMap(MethodDescription::parse, method -> method.name);

	public static final MapCodec<MethodDescription> FULL_CODEC = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), method -> method.name,
			JavaType.CLASS_CODEC.optional().fieldOf("class"), method -> method.containingClass,
			JavaType.CODEC.listOf().optional().fieldOf("parameters"), method -> method.parameters,
			JavaType.CODEC.optional().fieldOf("return"), method -> method.returnType,
			MethodDescription::new
	);

	public static final Codec<MethodDescription> CODEC = FULL_CODEC.asCodec().withAlternative(STRING_CODEC);
	public static final Codec<MethodDescription> WITH_CLASS_CODEC = CODEC.validate(MethodDescription::hasClass);

	public final String name;
	public final Optional<JavaType> containingClass;
	public final Optional<List<JavaType>> parameters;
	public final Optional<JavaType> returnType;

	private MethodDescription(String name, Optional<JavaType> containingClass, Optional<List<JavaType>> parameters, Optional<JavaType> returnType) {
		this.name = name;
		this.containingClass = containingClass;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	public boolean matches(MethodInsnNode node) {
		if (!this.name.equals(node.name))
			return false;

		if (this.containingClass.isPresent()) {
			if (!this.containingClass.get().internalName.equals(node.owner)) {
				return false;
			}
		}

		return this.parametersMatch(() -> Type.getArgumentTypes(node.desc))
				&& this.returnTypeMatches(() -> Type.getReturnType(node.desc));
	}



	public boolean matches(ClassNode clazz, MethodNode method) {
		if (!this.name.equals(method.name))
			return false;

		if (this.containingClass.isPresent()) {
			if (!this.containingClass.get().matches(clazz)) {
				return false;
			}
		}

		return this.parametersMatch(() -> Type.getArgumentTypes(method.desc))
				&& this.returnTypeMatches(() -> Type.getReturnType(method.desc));
	}

	public Either<Class<?>, ClassMissingReason> resolveClass() {
		if (!this.containingClass.isPresent()) {
			return Either.right(ClassMissingReason.NOT_SPECIFIED);
		}

		try {
			String name = this.containingClass.get().name;
			Class<?> clazz = Class.forName(name, false, this.getClass().getClassLoader());
			return Either.left(clazz);
		} catch (ClassNotFoundException ignored) {
			return Either.right(ClassMissingReason.NOT_FOUND);
		}
	}

	public Either<Method, MethodMissingReason> resolve() {
		Either<Class<?>, ClassMissingReason> result = this.resolveClass();
		if (result.isRight()) {
			return Either.right(result.right().methodReason);
		} else {
			Method method = this.resolveInClass(result.left());
			return method != null ? Either.left(method) : Either.right(MethodMissingReason.NOT_FOUND);
		}
	}

	@Nullable
	public Method resolveInClass(Class<?> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.getName().equals(this.name))
				continue;

			if (!this.parametersMatch(() -> Type.getArgumentTypes(method)))
				continue;

			if (!this.returnTypeMatches(() -> Type.getReturnType(method)))
				continue;

			return method;
		}

		return null;
	}

	private boolean parametersMatch(Supplier<Type[]> actualGetter) {
		if (!this.parameters.isPresent())
			return true;

		List<JavaType> expected = this.parameters.get();
		Type[] actual = actualGetter.get();

		if (expected.size() != actual.length)
			return false;

		for (int i = 0; i < actual.length; i++) {
			if (!expected.get(i).matches(actual[i])) {
				return false;
			}
		}

		return true;
	}

	private boolean returnTypeMatches(Supplier<Type> returnType) {
		return this.returnType.map(type -> type.matches(returnType.get())).orElse(true);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.returnType.ifPresent(type -> builder.append(type).append(' '));
		this.containingClass.ifPresent(clazz -> builder.append(clazz).append('.'));
		builder.append(this.name);
		this.parameters.ifPresent(params -> {
			builder.append('(');
			for (int i = 0; i < params.size(); i++) {
				JavaType param = params.get(i);
				builder.append(param);
				if (i + 1 < params.size()) {
					builder.append(", ");
				}
			}
			builder.append(')');
		});
		return builder.toString();
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
		JavaType clazz = JavaType.parseClass(className);
		if (clazz == null) {
			return CodecResult.error("Not a class: " + className);
		}

		String name = string.substring(lastDot + 1);
		MethodDescription method = new MethodDescription(name, Optional.of(clazz), Optional.empty(), Optional.empty());
		return CodecResult.success(method);
	}

	private static CodecResult<MethodDescription> hasClass(MethodDescription description) {
		if (description.containingClass.isPresent()) {
			return CodecResult.success(description);
		} else {
			return CodecResult.error("Class not specified: " + description);
		}
	}

	public enum ClassMissingReason {
		NOT_SPECIFIED(MethodMissingReason.CLASS_NOT_SPECIFIED),
		NOT_FOUND(MethodMissingReason.CLASS_NOT_FOUND);

		public final MethodMissingReason methodReason;

		ClassMissingReason(MethodMissingReason methodReason) {
			this.methodReason = methodReason;
		}
	}

	public enum MethodMissingReason {
		CLASS_NOT_SPECIFIED, CLASS_NOT_FOUND, NOT_FOUND;

		private final String humanReadable = this.name().toLowerCase(Locale.ROOT).replace('_', ' ');

		@Override
		public String toString() {
			return this.humanReadable;
		}
	}
}
