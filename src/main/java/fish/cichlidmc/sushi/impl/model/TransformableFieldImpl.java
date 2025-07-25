package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import org.glavo.classfile.FieldModel;
import org.glavo.classfile.FieldTransform;

import java.util.Optional;

public final class TransformableFieldImpl implements TransformableField {
	private final FieldModel model;
	private final TransformableClass owner;

	private FieldTransform rawTransform;

	public TransformableFieldImpl(FieldModel model, TransformableClass owner) {
		this.model = model;
		this.owner = owner;
	}

	@Override
	public FieldModel model() {
		return this.model;
	}

	@Override
	public TransformableClass owner() {
		return this.owner;
	}

	@Override
	public void transform(FieldTransform transform) {
		if (this.rawTransform == null) {
			this.rawTransform = transform;
		} else {
			this.rawTransform = this.rawTransform.andThen(transform);
		}
	}

	public Optional<FieldTransform> transform() {
		return Optional.ofNullable(this.rawTransform);
	}
}
