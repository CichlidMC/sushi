package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.requirement.RequirementCollector;

import java.lang.classfile.ClassModel;

/// Holds information about a class transformation.
public final class Transformation {
	public final ClassFileAccess classFile;
	public final boolean metadata;
	public final RequirementCollector requirements;

	private TransformableClassImpl clazz;

	public Transformation(ClassFileAccess classFile, boolean metadata, ClassModel initialModel) {
		this.classFile = classFile;
		this.metadata = metadata;
		this.requirements = new RequirementCollector();
		this.updateClass(initialModel);
	}

	public TransformableClassImpl clazz() {
		return this.clazz;
	}

	public void update(byte[] bytes) {
		this.updateClass(this.classFile.get().parse(bytes));
	}

	private void updateClass(ClassModel model) {
		this.clazz = new TransformableClassImpl(this, model, this.clazz);
	}
}
