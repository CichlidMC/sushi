package fish.cichlidmc.sushi.api.validation;

import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Optional;
import java.util.Set;

public interface ClassInfo {
	Set<AccessFlag> flags();

	Optional<MethodInfo> findMethod(String name, MethodTypeDesc desc);
}
