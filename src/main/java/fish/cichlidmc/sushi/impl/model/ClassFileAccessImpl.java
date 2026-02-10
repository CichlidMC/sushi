package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;

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
import java.lang.classfile.ClassHierarchyResolver;

public final class ClassFileAccessImpl implements ClassFileAccess {
	private static final AttributeMapperOption defaultAttributeMapper = AttributeMapperOption.of(_ -> null);
	private static final ClassHierarchyResolverOption defaultClassHierarchyResolver = ClassHierarchyResolverOption.of(ClassHierarchyResolver.defaultResolver());

	private final AttributeMapperOption attributeMapper;
	private final ClassHierarchyResolverOption hierarchyResolver;
	private final ConstantPoolSharingOption constantPoolSharing;
	private final DeadCodeOption deadCode;
	private final DeadLabelsOption deadLabels;
	private final DebugElementsOption debugElements;
	private final LineNumbersOption lineNumbers;
	private final ShortJumpsOption shortJumps;
	private final StackMapsOption stackMaps;
	private final AttributesProcessingOption attributesProcessing;

	private final ClassFile classFile;

	/// [jdk.internal.classfile.impl.ClassFileImpl#DEFAULT_CONTEXT]
	@SuppressWarnings("JavadocReference")
	public ClassFileAccessImpl(Iterable<? extends ClassFile.Option> options) {
		AttributeMapperOption attributeMapper = defaultAttributeMapper;
		ClassHierarchyResolverOption hierarchyResolver = defaultClassHierarchyResolver;
		ConstantPoolSharingOption constantPoolSharing = ConstantPoolSharingOption.SHARED_POOL;
		DeadCodeOption deadCode = DeadCodeOption.PATCH_DEAD_CODE;
		DeadLabelsOption deadLabels = DeadLabelsOption.FAIL_ON_DEAD_LABELS;
		DebugElementsOption debugElements = DebugElementsOption.PASS_DEBUG;
		LineNumbersOption lineNumbers = LineNumbersOption.PASS_LINE_NUMBERS;
		ShortJumpsOption shortJumps = ShortJumpsOption.FIX_SHORT_JUMPS;
		StackMapsOption stackMaps = StackMapsOption.STACK_MAPS_WHEN_REQUIRED;
		AttributesProcessingOption attributesProcessing = AttributesProcessingOption.PASS_ALL_ATTRIBUTES;

		for (ClassFile.Option option : options) {
			switch (option) {
				case AttributeMapperOption mapper -> attributeMapper = mapper;
				case ClassHierarchyResolverOption resolver -> hierarchyResolver = resolver;
				case ConstantPoolSharingOption sharing -> constantPoolSharing = sharing;
				case DeadCodeOption code -> deadCode = code;
				case DeadLabelsOption labels -> deadLabels = labels;
				case DebugElementsOption debug -> debugElements = debug;
				case LineNumbersOption numbers -> lineNumbers = numbers;
				case ShortJumpsOption jumps -> shortJumps = jumps;
				case StackMapsOption maps -> stackMaps = maps;
				case AttributesProcessingOption processing -> attributesProcessing = processing;
			}
		}

		this(
				attributeMapper, hierarchyResolver, constantPoolSharing, deadCode, deadLabels,
				debugElements, lineNumbers, shortJumps, stackMaps, attributesProcessing
		);
	}

	public ClassFileAccessImpl(AttributeMapperOption attributeMapper,
							   ClassHierarchyResolverOption hierarchyResolver,
							   ConstantPoolSharingOption constantPoolSharing,
							   DeadCodeOption deadCode,
							   DeadLabelsOption deadLabels,
							   DebugElementsOption debugElements,
							   LineNumbersOption lineNumbers,
							   ShortJumpsOption shortJumps,
							   StackMapsOption stackMaps,
							   AttributesProcessingOption attributesProcessing) {
		this.attributeMapper = attributeMapper;
		this.hierarchyResolver = hierarchyResolver;
		this.constantPoolSharing = constantPoolSharing;
		this.deadCode = deadCode;
		this.deadLabels = deadLabels;
		this.debugElements = debugElements;
		this.lineNumbers = lineNumbers;
		this.shortJumps = shortJumps;
		this.stackMaps = stackMaps;
		this.attributesProcessing = attributesProcessing;

		this.classFile = ClassFile.of(
				attributeMapper, hierarchyResolver, constantPoolSharing,
				deadCode, deadLabels, debugElements, lineNumbers,
				shortJumps, stackMaps, attributesProcessing
		);
	}

	@Override
	public ClassFile get() {
		return this.classFile;
	}

	@Override
	public AttributeMapperOption attributeMapper() {
		return this.attributeMapper;
	}

	@Override
	public ClassHierarchyResolverOption hierarchyResolver() {
		return this.hierarchyResolver;
	}

	@Override
	public ConstantPoolSharingOption constantPoolSharing() {
		return this.constantPoolSharing;
	}

	@Override
	public DeadCodeOption deadCode() {
		return this.deadCode;
	}

	@Override
	public DeadLabelsOption deadLabels() {
		return this.deadLabels;
	}

	@Override
	public DebugElementsOption debugElements() {
		return this.debugElements;
	}

	@Override
	public LineNumbersOption lineNumbers() {
		return this.lineNumbers;
	}

	@Override
	public ShortJumpsOption shortJumps() {
		return this.shortJumps;
	}

	@Override
	public StackMapsOption stackMaps() {
		return this.stackMaps;
	}

	@Override
	public AttributesProcessingOption attributesProcessing() {
		return this.attributesProcessing;
	}
}
