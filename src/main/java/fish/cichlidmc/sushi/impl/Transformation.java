package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.requirement.RequirementCollector;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

/// Holds information about a class transformation.
public final class Transformation {
	public final ClassFile context;
	public final boolean metadata;
	public final RequirementCollector requirements;

	private TransformableClassImpl clazz;

	public Transformation(ClassFile context, boolean metadata, ClassModel initialModel) {
		this.context = context;
		this.metadata = metadata;
		this.requirements = new RequirementCollector();
		this.updateClass(initialModel);
	}

	public TransformableClassImpl clazz() {
		return this.clazz;
	}

	public void update(byte[] bytes) {
		this.updateClass(this.context.parse(bytes));
	}

	private void updateClass(ClassModel model) {
		this.clazz = new TransformableClassImpl(this, model, this.clazz);
	}
}
