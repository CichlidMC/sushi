package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import org.glavo.classfile.ClassModel;

import java.util.Optional;

public final class TransformContextImpl implements TransformContext {
	private final TransformableClassImpl clazz;
	private final Optional<Validation> validation;
	private final boolean addMetadata;

	private Id currentId;
	private boolean finished;

	public TransformContextImpl(ClassModel clazz, Optional<Validation> validation, boolean addMetadata) {
		this.clazz = new TransformableClassImpl(clazz, this);
		this.validation = validation;
		this.addMetadata = addMetadata;
	}

	public void setCurrentId(Id currentId) {
		this.currentId = currentId;
	}

	public void finish() {
		this.finished = true;
	}

	@Override
	public TransformableClassImpl clazz() {
		return this.clazz;
	}

	@Override
	public Optional<Validation> validation() {
		return this.validation;
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
