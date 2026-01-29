package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.util.MethodGeneration;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.SequencedMap;

/// A class that is open for transformation.
///
/// May be [transformed directly][DirectlyTransformable], or
/// [individual][#methods()] [elements][#fields()] may be targeted more granularly.
public sealed interface TransformableClass extends HasAttachments, DirectlyTransformable<DirectTransform.Class> permits TransformableClassImpl {
	ClassModel model();

	default ClassDesc desc() {
		return this.model().thisClass().asSymbol();
	}

	/// @return an immutable view of the methods of this class
	SequencedMap<MethodKey, TransformableMethod> methods();

	/// @return an immutable view of the fields of this class
	SequencedMap<FieldKey, TransformableField> fields();

	/// Create an identifiable name for a method that is guaranteed to be unique.
	///
	/// The created name will be remembered to ensure uniqueness of future invocations.
	/// @param prefix a prefix to include in the name
	/// @param owner the ID of the transformer that caused the method to be created
	/// @see MethodGeneration
	String createUniqueMethodName(String prefix, Id owner);
}
