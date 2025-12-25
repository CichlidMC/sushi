package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.util.SequencedMap;

/// Provides an immutable view of a class and its elements, while allowing defining transformations to apply later.
public sealed interface TransformableClass extends HasAttachments permits TransformableClassImpl {
	ClassModel model();

	default ClassDesc desc() {
		return this.model().thisClass().asSymbol();
	}

	/// @return an immutable view of the methods of this class
	SequencedMap<MethodKey, TransformableMethod> methods();

	/// @return an immutable view of the fields of this class
	SequencedMap<FieldKey, TransformableField> fields();

	/// Register a new transform to apply directly, skipping Sushi. **Here be dragons!** Use responsibly.
	///
	/// Your transform will be invoked after Sushi finishes its own transformations.
	void transform(ClassTransform transform);
}
