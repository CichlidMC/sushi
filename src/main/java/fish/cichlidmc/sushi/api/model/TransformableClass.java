package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.transform.sliced.model.SlicedTransformableClass;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;

import java.lang.constant.ClassDesc;
import java.util.List;

/**
 * Provides an immutable view of a class and its elements, while allowing defining transformations to apply later.
 */
public sealed interface TransformableClass permits TransformableClassImpl, SlicedTransformableClass {
	ClassModel model();

	default ClassDesc desc() {
		return this.model().thisClass().asSymbol();
	}

	List<? extends TransformableMethod> methods();

	List<? extends TransformableField> fields();

	/**
	 * Register a new transform to apply directly, skipping Sushi. <strong>Here be dragons!</strong> Use responsibly.
	 * <p>
	 * Your transform will be invoked after Sushi finishes its own transformations.
	 */
	void transform(ClassTransform transform);
}
