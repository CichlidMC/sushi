package fish.cichlidmc.sushi.impl.validation;

import fish.cichlidmc.sushi.api.validation.MethodInfo;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.util.Set;

public record RuntimeMethodInfo(Method method) implements MethodInfo {
	@Override
	public Set<AccessFlag> flags() {
		return this.method.accessFlags();
	}
}
