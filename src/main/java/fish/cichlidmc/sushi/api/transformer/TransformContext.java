package fish.cichlidmc.sushi.api.transformer;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;

import java.lang.classfile.ClassFile;

/// Context about the currently occurring transformation.
public sealed interface TransformContext permits TransformContextImpl {
	/// @return the [TransformableClass] currently being transformed.
	TransformableClass target();

	/// Register a new [Requirement] that must be met for transformations to be correct.
	void require(Requirement requirement);

	/// @return true if metadata of any kind should be added to transformed classes
	boolean addMetadata();

	/// @return access to the [ClassFile] used for parsing and transforming, as well as all its options
	ClassFileAccess classFile();

	/// @return the [Id] of the transformer currently being applied.
	Id transformerId();
}
