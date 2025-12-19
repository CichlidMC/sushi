package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

import java.lang.classfile.ClassModel;
import java.util.ArrayList;
import java.util.List;

public final class TransformContextImpl implements TransformContext {
	private final TransformableClassImpl clazz;
	private final List<Requirements.Owned> requirements;
	private final boolean addMetadata;

	private Id currentId;
	private List<Requirement> currentRequirements;
	private boolean finished;

	public TransformContextImpl(ClassModel clazz, boolean addMetadata) {
		this.clazz = new TransformableClassImpl(clazz, this);
		this.requirements = new ArrayList<>();
		this.addMetadata = addMetadata;
	}

	public void setCurrentId(Id currentId) {
		if (this.currentId != null) {
			this.requirements.add(new Requirements.Owned(this.currentId, this.currentRequirements));
		}

		this.currentId = currentId;
		this.currentRequirements = new ArrayList<>();
	}

	public void finish() {
		this.finished = true;
		this.requirements.add(new Requirements.Owned(this.currentId, this.currentRequirements));
	}

	public Requirements collectRequirements() {
		return Requirements.of(this.requirements);
	}

	@Override
	public TransformableClassImpl clazz() {
		return this.clazz;
	}

	@Override
	public void require(Requirement requirement) {
		this.currentRequirements.add(requirement);
	}

	@Override
	public boolean addMetadata() {
		return this.addMetadata;
	}

	@Override
	public Id transformerId() {
		if (this.finished) {
			throw new IllegalStateException("transformerId called too late!");
		}

		if (this.currentId == null) {
			throw new IllegalStateException("currentId is not set");
		}

		return this.currentId;
	}
}
