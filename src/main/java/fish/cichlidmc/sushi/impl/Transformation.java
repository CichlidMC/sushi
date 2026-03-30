package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.api.transformer.ClassOutput;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.requirement.RequirementCollector;

import java.lang.classfile.ClassModel;
import java.util.Optional;

/// Holds information about a class transformation.
public final class Transformation {
	public final ClassFileAccess classFile;
	public final boolean metadata;
	public final RequirementCollector requirements;
	public final Optional<ClassOutput> classOutput;

	private TransformableClassImpl clazz;

	public Transformation(ClassFileAccess classFile, boolean metadata, Optional<ClassOutput> classOutput, ClassModel initialModel) {
		this.classFile = classFile;
		this.metadata = metadata;
		this.classOutput = classOutput;
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
