package fish.cichlidmc.sushi.impl.validation;

import fish.cichlidmc.sushi.api.validation.FieldInfo;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.Set;

public record RuntimeFieldInfo(Field field) implements FieldInfo {
	@Override
	public Set<AccessFlag> flags() {
		return this.field.accessFlags();
	}
}
