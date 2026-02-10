package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.requirement.RequirementCollector;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

/// Holds information about a class transformation.
public final class Transformation {
	public final ClassFile context;
	public final boolean metadata;
	public final ClassFileAccess classFile;
	public final RequirementCollector requirements;

	private TransformableClassImpl clazz;

	public Transformation(ClassFile context, boolean metadata, ClassFileAccess classFile, ClassModel initialModel) {
		this.context = context;
		this.metadata = metadata;
		this.classFile = classFile;
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
