package fish.cichlidmc.sushi.impl.validation;

import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.validation.ClassInfo;
import fish.cichlidmc.sushi.api.validation.FieldInfo;
import fish.cichlidmc.sushi.api.validation.MethodInfo;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record RuntimeClassInfo(RuntimeValidation validation, Class<?> clazz) implements ClassInfo {
	@Override
	public Optional<ClassDesc> superclass() {
		return Optional.ofNullable(this.clazz.getSuperclass()).map(ClassDescs::of);
	}

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

		for (Method method : this.clazz.getDeclaredMethods()) {
			if (!method.getName().equals(name))
				continue;

			if (!Arrays.equals(paramArray, method.getParameterTypes()))
				continue;

			if (returnType != method.getReturnType())
				continue;

			return Optional.of(new RuntimeMethodInfo(method));
		}

		return Optional.empty();
	}

	@Override
	public Optional<FieldInfo> findField(String name, ClassDesc type) {
		Class<?> typeClass = this.validation.findClass(type).map(RuntimeClassInfo::clazz).orElse(null);
		if (typeClass == null) {
			return Optional.empty();
		}

		for (Field field : this.clazz.getDeclaredFields()) {
			if (!field.getName().equals(name))
				continue;

			if (typeClass != field.getType())
				continue;

			return Optional.of(new RuntimeFieldInfo(field));
		}

		return Optional.empty();
	}
}
