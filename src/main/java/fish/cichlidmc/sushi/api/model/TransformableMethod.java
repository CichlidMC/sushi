package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;

import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.constant.ClassDesc;
import java.util.Optional;

public sealed interface TransformableMethod extends HasAttachments permits TransformableMethodImpl {
	MethodModel model();

	default ClassDesc returnType() {
		return this.model().methodTypeSymbol().returnType();
	}

	default ClassDesc[] parameterTypes() {
		return this.model().methodTypeSymbol().parameterArray();
	}

	TransformableClass owner();

	/// The code of this method, if present. A method won't have code if it's abstract or native.
	Optional<TransformableCode> code();
	
	/// Register a new transform to apply directly, **skipping Sushi! Here be dragons!**
	/// Using this to modify code is highly discouraged and likely to cause incompatibilities.
	///
	/// Your transform will be invoked after Sushi finishes its own transformations.
	/// @see #code()
	void transform(MethodTransform transform);

	@Override
	String toString();
}
