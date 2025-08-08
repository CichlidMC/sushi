package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import org.glavo.classfile.ClassModel;

import java.util.Optional;

public final class TransformContextImpl implements TransformContext {
	private final TransformableClassImpl clazz;
	private final Optional<Validation> validation;

	private Id currentId;

	public TransformContextImpl(ClassModel clazz, Optional<Validation> validation) {
		this.clazz = new TransformableClassImpl(clazz, this);
		this.validation = validation;
	}

	public void setCurrentId(Id currentId) {
		this.currentId = currentId;
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
	public Id transformerId() {
		if (this.currentId == null) {
			throw new IllegalStateException("currentId is not set");
		}

		return this.currentId;
	}
}
