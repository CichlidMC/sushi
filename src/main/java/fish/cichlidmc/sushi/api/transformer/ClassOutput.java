package fish.cichlidmc.sushi.api.transformer;

import java.lang.classfile.ClassBuilder;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

/// Allows transformers to output additional classes during transformations.
/// @see TransformContext#classOutput()
@FunctionalInterface
public interface ClassOutput {
	/// Define a new class to output.
	///
	/// Care should be taken to make sure defined classes have unique names.
	/// If they do not, an exception is likely to be thrown.
	///
	/// @param desc the descriptor of the class to generate
	/// @param consumer a consumer to build the class, which may be invoked at a later time
	/// @throws IllegalArgumentException if the defined class is not unique
	void define(ClassDesc desc, Consumer<ClassBuilder> consumer) throws IllegalArgumentException;
}
