package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;

import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.util.Optional;

/// A method in a class open to transformation.
///
/// If a method has [code][#code()], [TransformableCode] may be used to define safe, granular transformations.
///
/// May also be [transformed directly][DirectlyTransformable].
public sealed interface TransformableMethod extends HasAttachments, DirectlyTransformable<DirectTransform.Method> permits TransformableMethodImpl {
	MethodModel model();

	MethodKey key();

	default ClassDesc returnType() {
		return this.model().methodTypeSymbol().returnType();
	}

	default ClassDesc[] parameterTypes() {
		return this.model().methodTypeSymbol().parameterArray();
	}

	TransformableClass owner();

	/// The code of this method, if present. A method won't have code if it's abstract or native.
	Optional<TransformableCode> code();

	@Override
	String toString();
}
