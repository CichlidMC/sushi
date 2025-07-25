package fish.cichlidmc.sushi.impl.validation;

import fish.cichlidmc.sushi.api.validation.ClassInfo;
import fish.cichlidmc.sushi.api.validation.MethodInfo;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record RuntimeClassInfo(RuntimeValidation validation, Class<?> clazz) implements ClassInfo {
	@Override
	public Set<AccessFlag> flags() {
		return this.clazz.accessFlags();
	}

	@Override
	public Optional<MethodInfo> findMethod(String name, MethodTypeDesc desc) {
		Class<?> returnType = this.validation.findClass(desc.returnType()).map(RuntimeClassInfo::clazz).orElse(null);
		if (returnType == null) {
			return Optional.empty();
		}

		List<ClassDesc> paramList = desc.parameterList();
		Class<?>[] paramArray = new Class[paramList.size()];

		for (int i = 0; i < paramList.size(); i++) {
			ClassDesc param = paramList.get(i);

			Class<?> paramClass = this.validation.findClass(param).map(RuntimeClassInfo::clazz).orElse(null);
			if (paramClass == null) {
				return Optional.empty();
			}

			paramArray[i] = paramClass;
		}

		try {
			Method method = this.clazz.getDeclaredMethod(name, paramArray);
			return Optional.of(new RuntimeMethodInfo(method));
		} catch (NoSuchMethodException ignored) {
			return Optional.empty();
		}
	}
}
