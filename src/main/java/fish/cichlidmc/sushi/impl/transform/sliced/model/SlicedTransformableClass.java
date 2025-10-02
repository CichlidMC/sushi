package fish.cichlidmc.sushi.impl.transform.sliced.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;

import java.util.List;

public final class SlicedTransformableClass implements TransformableClass {
	private final TransformableClass wrapped;
	private final List<TransformableMethod> wrappedMethods;

	public SlicedTransformableClass(TransformableClass wrapped, CodeSlicer slicer) {
		this.wrapped = wrapped;
		this.wrappedMethods = wrapped.methods().stream().map(method -> (TransformableMethod) new SlicedTransformableMethod(method, slicer)).toList();
	}

	@Override
	public ClassModel model() {
		return this.wrapped.model();
	}

	@Override
	public List<TransformableMethod> methods() {
		return this.wrappedMethods;
	}

	@Override
	public List<TransformableField> fields() {
		return this.wrapped.fields();
	}

	@Override
	public void transform(ClassTransform transform) {
		this.wrapped.transform(transform);
	}

	@Override
	public AttachmentMap attachments() {
		return this.wrapped.attachments();
	}
}
