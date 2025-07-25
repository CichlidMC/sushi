package fish.cichlidmc.sushi.api.validation;

import fish.cichlidmc.sushi.impl.validation.RuntimeValidation;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public interface Validation {
	Optional<? extends ClassInfo> findClass(ClassDesc desc);

	default Optional<? extends MethodInfo> findMethod(DirectMethodHandleDesc desc) {
		return this.findClass(desc.owner()).flatMap(owner -> owner.findMethod(desc.methodName(), desc.invocationType()));
	}

	/**
	 * Create a Validation instance based on the current runtime classes available to the given Lookup.
	 * @see MethodHandles#lookup()
	 */
	static Validation runtime(MethodHandles.Lookup lookup) {
		return new RuntimeValidation(lookup);
	}
}
