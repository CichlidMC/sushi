package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;

import java.lang.classfile.FieldModel;

/// A field in a class open to transformation.
///
/// Currently, Sushi only supports [direct transformation][DirectlyTransformable] of fields.
public sealed interface TransformableField extends HasAttachments, DirectlyTransformable<DirectTransform.Field> permits TransformableFieldImpl {
	FieldModel model();

	FieldKey key();

	TransformableClass owner();

	@Override
	String toString();
}
