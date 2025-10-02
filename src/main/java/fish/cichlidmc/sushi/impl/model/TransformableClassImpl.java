package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;

import java.util.List;

public final class TransformableClassImpl implements TransformableClass {
	public final TransformContextImpl context;

	private final ClassModel model;
	private final List<TransformableMethod> methods;
	private final List<TransformableField> fields;
	private final AttachmentMap attachments;

	private ClassTransform directTransform;

	public TransformableClassImpl(ClassModel model, TransformContextImpl context) {
		this.model = model;
		this.context = context;
		this.methods = model.methods().stream().map(method -> (TransformableMethod) new TransformableMethodImpl(method, this)).toList();
		this.fields = model.fields().stream().map(field -> (TransformableField) new TransformableFieldImpl(field, this)).toList();
		this.attachments = AttachmentMap.create();
	}

	@Override
	public ClassModel model() {
		return this.model;
	}

	@Override
	public List<TransformableMethod> methods() {
		return this.methods;
	}

	@Override
	public List<TransformableField> fields() {
		return this.fields;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transform(ClassTransform transform) {
		Id owner = this.context.transformerId();
		transform = new IdentifiedTransform.Class(owner, transform);

		if (this.directTransform == null) {
			this.directTransform = transform;
		} else {
			this.directTransform = this.directTransform.andThen(transform);
		}
	}

	public ClassTransform append(ClassTransform base) {
		return this.directTransform == null ? base : base.andThen(this.directTransform);
	}
}
