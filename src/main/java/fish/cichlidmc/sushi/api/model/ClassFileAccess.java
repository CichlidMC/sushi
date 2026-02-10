package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.impl.model.ClassFileAccessImpl;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFile.AttributeMapperOption;
import java.lang.classfile.ClassFile.AttributesProcessingOption;
import java.lang.classfile.ClassFile.ClassHierarchyResolverOption;
import java.lang.classfile.ClassFile.ConstantPoolSharingOption;
import java.lang.classfile.ClassFile.DeadCodeOption;
import java.lang.classfile.ClassFile.DeadLabelsOption;
import java.lang.classfile.ClassFile.DebugElementsOption;
import java.lang.classfile.ClassFile.LineNumbersOption;
import java.lang.classfile.ClassFile.ShortJumpsOption;
import java.lang.classfile.ClassFile.StackMapsOption;

/// Wrapper around a [ClassFile], allowing for reading individual options.
public sealed interface ClassFileAccess permits ClassFileAccessImpl {
	ClassFile get();

	AttributeMapperOption attributeMapper();

	ClassHierarchyResolverOption hierarchyResolver();

	ConstantPoolSharingOption constantPoolSharing();

	DeadCodeOption deadCode();

	DeadLabelsOption deadLabels();

	DebugElementsOption debugElements();

	LineNumbersOption lineNumbers();

	ShortJumpsOption shortJumps();

	StackMapsOption stackMaps();

	AttributesProcessingOption attributesProcessing();

	/// Create a new [ClassFileAccess] with the given options.
	/// Anything omitted will have a default value.
	static ClassFileAccess of(Iterable<? extends ClassFile.Option> options) {
		return new ClassFileAccessImpl(options);
	}
}
