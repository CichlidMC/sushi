package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.registry.content.SushiRequirements;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FieldRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FullyDefinedRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.InterpretedRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementStack;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.MalformedRequirementsException;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.RequirementInterpretationException;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.UnmetRequirementException;
import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record RuntimeRequirementInterpreters(MethodHandles.Lookup lookup) {
	public void setup(RequirementInterpreters interpreters) {
		interpreters.register(SushiRequirements.CLASS, this::interpretClass);
		interpreters.register(SushiRequirements.FIELD, this::interpretField);
		interpreters.register(SushiRequirements.METHOD, this::interpretMethod);
		interpreters.register(SushiRequirements.FLAGS, this::interpretFlags);
		interpreters.register(SushiRequirements.FULLY_DEFINED, this::interpretFullyDefined);
	}

	private Class<?> interpretClass(ClassRequirement requirement, RequirementStack previous) throws UnmetRequirementException {
		try {
			return requirement.desc().resolveConstantDesc(this.lookup);
		} catch (ReflectiveOperationException ignored) {
			throw new UnmetRequirementException("Class not found: " + ClassDescs.fullName(requirement.desc()));
		}
	}

	private Field interpretField(FieldRequirement requirement, RequirementStack previous) throws RequirementInterpretationException {
		Class<?> clazz = previous.findHighest(SushiRequirements.CLASS).orElseThrow(
				() -> new MalformedRequirementsException("FieldRequirement must be chained after a ClassRequirement")
		).getChecked(Class.class);

		String name = requirement.name();
		ClassDesc type = requirement.type();

		for (Field field : clazz.getDeclaredFields()) {
			if (!name.equals(field.getName()))
				continue;

			if (!ClassDescs.equals(type, field.getType()))
				continue;

			return field;
		}

		throw new UnmetRequirementException("Field not found: " + type + ' ' + name);
	}

	private Method interpretMethod(MethodRequirement requirement, RequirementStack previous) throws RequirementInterpretationException {
		Class<?> clazz = previous.findHighest(SushiRequirements.CLASS).orElseThrow(
				() -> new MalformedRequirementsException("MethodRequirement must be chained after a ClassRequirement")
		).getChecked(Class.class);

		String name = requirement.name();
		MethodTypeDesc desc = requirement.desc();

		for (Method method : clazz.getDeclaredMethods()) {
			if (!name.equals(method.getName()))
				continue;

			if (!desc.equals(descOf(method)))
				continue;

			return method;
		}

		throw new UnmetRequirementException("Method not found: " + formatMethod(name, desc));
	}

	private Set<AccessFlag> interpretFlags(FlagsRequirement requirement, RequirementStack previous) throws RequirementInterpretationException {
		Set<AccessFlag> flags = previous.findHighest(RuntimeRequirementInterpreters::hasFlags)
				.map(InterpretedRequirement::interpreted)
				.flatMap(interpreted -> switch (interpreted) {
					case Class<?> clazz -> Optional.of(clazz.accessFlags());
					case Method method -> Optional.of(method.accessFlags());
					case Field field -> Optional.of(field.accessFlags());
					default -> Optional.empty();
				})
				.orElseThrow(() -> new MalformedRequirementsException("FlagsRequirement must be chained after an object that has flags"));

		Set<org.glavo.classfile.AccessFlag> set = convert(flags);
		Set<org.glavo.classfile.AccessFlag> missing = EnumSet.noneOf(org.glavo.classfile.AccessFlag.class);
		Set<org.glavo.classfile.AccessFlag> excess = EnumSet.noneOf(org.glavo.classfile.AccessFlag.class);

		for (FlagsRequirement.Entry entry : requirement.flags()) {
			org.glavo.classfile.AccessFlag flag = entry.flag();

			switch (entry.mode()) {
				case REQUIRED -> {
					if (!set.contains(flag)) {
						missing.add(flag);
					}
				}
				case FORBIDDEN -> {
					if (set.contains(flag)) {
						excess.add(flag);
					}
				}
			}
		}

		if (missing.isEmpty() && excess.isEmpty())
			return flags;

		StringBuilder message = new StringBuilder("Incorrect flags; ");

		if (!missing.isEmpty()) {
			message.append(missing.size()).append(" flags are missing: ").append(missing);
		}

		if (!excess.isEmpty()) {
			if (!missing.isEmpty()) {
				message.append("; ");
			}

			message.append(missing.size()).append(" flags are forbidden but present: ").append(excess);
		}

		throw new UnmetRequirementException(message.toString());
	}

	private Void interpretFullyDefined(FullyDefinedRequirement ignored, RequirementStack previous) throws RequirementInterpretationException {
		Class<?> clazz = previous.findHighest(SushiRequirements.CLASS).orElseThrow(
				() -> new MalformedRequirementsException("FullyDefinedRequirement must be chained after a ClassRequirement")
		).getChecked(Class.class);

		Set<String> problems = new HashSet<>();

		for (Method method : clazz.getMethods()) {
			if (Modifier.isAbstract(method.getModifiers())) {
				problems.add(formatMethod(method.getName(), descOf(method)));
			}
		}

		if (problems.isEmpty())
			return null;

		throw new UnmetRequirementException(
				"One or more methods are abstract: " + problems.stream().collect(Collectors.joining("], [", "[", "]"))
		);
	}

	private static MethodTypeDesc descOf(Method method) {
		ClassDesc returnType = ClassDescs.of(method.getReturnType());
		ClassDesc[] params = Arrays.stream(method.getParameterTypes())
				.map(ClassDescs::of)
				.toArray(ClassDesc[]::new);

		return MethodTypeDesc.of(returnType, params);
	}

	private static String formatMethod(String name, MethodTypeDesc desc) {
		return String.format(
				"%s %s(%s)",
				desc.returnType().displayName(), name,
				desc.parameterList().stream()
						.map(ClassDesc::displayName)
						.collect(Collectors.joining(", "))
		);
	}

	private static boolean hasFlags(InterpretedRequirement requirement) {
		return requirement.is(SushiRequirements.CLASS) || requirement.is(SushiRequirements.FIELD) || requirement.is(SushiRequirements.METHOD);
	}

	private static Set<org.glavo.classfile.AccessFlag> convert(Set<java.lang.reflect.AccessFlag> flags) {
		Set<org.glavo.classfile.AccessFlag> set = EnumSet.noneOf(org.glavo.classfile.AccessFlag.class);
		for (java.lang.reflect.AccessFlag flag : flags) {
			try {
				set.add(org.glavo.classfile.AccessFlag.valueOf(flag.name()));
			} catch (IllegalArgumentException ignored) {
				throw new RuntimeException("Flag " + flag.name() + " is unknown to the ClassFile API");
			}
		}
		return set;
	}
}
