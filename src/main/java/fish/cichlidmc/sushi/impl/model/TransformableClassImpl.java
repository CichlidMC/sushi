package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;

import java.util.List;

public final class TransformableClassImpl implements TransformableClass {
	public final TransformContextImpl context;

	private final ClassModel model;
	private final List<TransformableMethodImpl> methods;
	private final List<TransformableFieldImpl> fields;

	private ClassTransform rawTransform;

	public TransformableClassImpl(ClassModel model, TransformContextImpl context) {
		this.model = model;
		this.context = context;
		this.methods = model.methods().stream().map(method -> new TransformableMethodImpl(method, this)).toList();
		this.fields = model.fields().stream().map(field -> new TransformableFieldImpl(field, this)).toList();
	}

	@Override
	public ClassModel model() {
		return this.model;
	}

	@Override
	public List<TransformableMethodImpl> methods() {
		return this.methods;
	}

	@Override
	public List<TransformableFieldImpl> fields() {
		return this.fields;
	}

	@Override
	public void transform(ClassTransform transform) {
		if (this.rawTransform == null) {
			this.rawTransform = transform;
		} else {
			this.rawTransform = this.rawTransform.andThen(transform);
		}
	}

	public ClassTransform append(ClassTransform base) {
		return this.rawTransform == null ? base : base.andThen(this.rawTransform);
	}
}
