package fish.cichlidmc.sushi.impl.validation;

import fish.cichlidmc.sushi.api.validation.Validation;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public record RuntimeValidation(MethodHandles.Lookup lookup) implements Validation {
	@Override
	public Optional<RuntimeClassInfo> findClass(ClassDesc desc) {
		try {
			Class<?> resolved = desc.resolveConstantDesc(this.lookup);
			return Optional.of(new RuntimeClassInfo(this, resolved));
		} catch (ReflectiveOperationException ignored) {
			return Optional.empty();
		}
	}
}
