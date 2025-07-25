package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.transform.sliced.model.SlicedTransformableMethod;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodTransform;

import java.lang.constant.ClassDesc;
import java.util.Optional;

public sealed interface TransformableMethod permits TransformableMethodImpl, SlicedTransformableMethod {
	MethodModel model();

	default ClassDesc returnType() {
		return this.model().methodTypeSymbol().returnType();
	}

	default ClassDesc[] parameterTypes() {
		return this.model().methodTypeSymbol().parameterArray();
	}

	TransformableClass owner();

	/**
	 * The code of this method, if present. A method won't have code if it's abstract or native.
	 */
	Optional<? extends TransformableCode> code();

	/**
	 * Register a new transform to apply directly, <strong>skipping Sushi! Here be dragons!</strong>
	 * Using this to modify code is highly discouraged and likely to cause incompatibilities.
	 * <p>
	 * Your transform will be invoked after Sushi finishes its own transformations.
	 * @see #code()
	 */
	void transform(MethodTransform transform);
}
