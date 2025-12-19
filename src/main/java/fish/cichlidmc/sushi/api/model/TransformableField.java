package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;

import java.lang.classfile.FieldModel;
import java.lang.classfile.FieldTransform;

public sealed interface TransformableField extends HasAttachments permits TransformableFieldImpl {
	FieldModel model();

	TransformableClass owner();

	/// Register a new [FieldTransform] for Sushi to run.
	/// There's no hand-holding here, take care and be responsible.
	void transform(FieldTransform transform);

	@Override
	String toString();
}
