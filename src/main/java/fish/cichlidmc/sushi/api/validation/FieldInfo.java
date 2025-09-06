package fish.cichlidmc.sushi.api.validation;

import java.lang.reflect.AccessFlag;
import java.util.Set;

public interface FieldInfo {
	Set<AccessFlag> flags();
}
