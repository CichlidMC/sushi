package fish.cichlidmc.sushi.api.validation;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Optional;
import java.util.Set;

public interface ClassInfo {
	/**
	 * @return the superclass of this class. Empty for interfaces, primitives, and {@link Object}.
	 */
	Optional<ClassDesc> superclass();

	Set<AccessFlag> flags();

	Optional<MethodInfo> findMethod(String name, MethodTypeDesc desc);

	Optional<FieldInfo> findField(String name, ClassDesc type);
}
