package fish.cichlidmc.sushi.impl.transform.sliced.model;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;

import java.util.List;

public final class SlicedTransformableClass implements TransformableClass {
	private final TransformableClass wrapped;
	private final List<SlicedTransformableMethod> wrappedMethods;

	public SlicedTransformableClass(TransformableClass wrapped, CodeSlicer slicer) {
		this.wrapped = wrapped;
		this.wrappedMethods = wrapped.methods().stream().map(method -> new SlicedTransformableMethod(method, slicer)).toList();
	}

	@Override
	public ClassModel model() {
		return this.wrapped.model();
	}

	@Override
	public List<SlicedTransformableMethod> methods() {
		return this.wrappedMethods;
	}

	@Override
	public List<? extends TransformableField> fields() {
		return this.wrapped.fields();
	}

	@Override
	public void transform(ClassTransform transform) {
		this.wrapped.transform(transform);
	}
}
