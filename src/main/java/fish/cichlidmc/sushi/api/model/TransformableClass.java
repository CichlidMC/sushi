package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.util.List;

/// Provides an immutable view of a class and its elements, while allowing defining transformations to apply later.
public sealed interface TransformableClass extends HasAttachments permits TransformableClassImpl {
	ClassModel model();

	default ClassDesc desc() {
		return this.model().thisClass().asSymbol();
	}

	List<TransformableMethod> methods();

	List<TransformableField> fields();

	/// Register a new transform to apply directly, skipping Sushi. **Here be dragons!** Use responsibly.
	///
	/// Your transform will be invoked after Sushi finishes its own transformations.
	void transform(ClassTransform transform);
}
