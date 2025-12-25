package fish.cichlidmc.sushi.api.model.key;

import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.classfile.FieldModel;
import java.lang.constant.ClassDesc;

/// Uniquely identifies a field in a class.
public record FieldKey(String name, ClassDesc type) {
	@Override
	public String toString() {
		return ClassDescs.fullName(this.type) + ' ' + this.name;
	}

	/// @return a new [FieldKey] representing the given [FieldModel]
	public static FieldKey of(FieldModel field) {
		return new FieldKey(field.fieldName().stringValue(), field.fieldTypeSymbol());
	}
}
