package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.transformer.DirectTransformContextImpl;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodTransform;

/// Factory for a transform that will be applied directly.
public sealed interface DirectTransform {
	@FunctionalInterface
	non-sealed interface Class extends DirectTransform {
		ClassTransform create(Context context);
	}

	@FunctionalInterface
	non-sealed interface Field extends DirectTransform {
		FieldTransform create(Context.Field context);
	}

	@FunctionalInterface
	non-sealed interface Method extends DirectTransform {
		MethodTransform create(Context.Method context);
	}

	@FunctionalInterface
	non-sealed interface Code extends DirectTransform {
		CodeTransform create(Context.Code context);
	}

	/// Context available to direct transforms.
	sealed interface Context permits Context.Member, DirectTransformContextImpl {
		/// @return the context of the transformer that registered this transform
		TransformContext transformContext();

		sealed interface Member extends Context permits Field, Method, DirectTransformContextImpl.MemberImpl {
			/// @return the builder for the class currently being transformed
			ClassBuilder classBuilder();
		}

		sealed interface Field extends Member permits DirectTransformContextImpl.FieldImpl {
			/// @return the field currently being transformed
			TransformableField field();
		}

		sealed interface Method extends Member permits Code, DirectTransformContextImpl.MethodImpl {
			/// @return the method currently being transformed
			TransformableMethod method();
		}

		sealed interface Code extends Method permits DirectTransformContextImpl.CodeImpl {
			/// @return the builder for the method currently being transformed
			MethodBuilder methodBuilder();

			/// @return the code currently being transformed
			TransformableCode code();
		}
	}
}
