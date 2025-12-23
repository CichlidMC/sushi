package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.impl.requirement.RequirementCollector;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

/// Holds information about a class transformation.
public final class Transformation {
	public final ClassFile context;
	public final boolean metadata;
	public final RequirementCollector requirements;

	private ClassModel model;

	public Transformation(ClassFile context, ClassModel initialModel, boolean metadata) {
		this.context = context;
		this.model = initialModel;
		this.metadata = metadata;
		this.requirements = new RequirementCollector();
	}

	public ClassModel model() {
		return this.model;
	}

	public void updateModel(byte[] bytes) {
		this.model = this.context.parse(bytes);
	}
}
