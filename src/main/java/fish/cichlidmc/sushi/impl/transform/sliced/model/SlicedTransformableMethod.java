package fish.cichlidmc.sushi.impl.transform.sliced.model;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodTransform;

import java.util.Optional;

public final class SlicedTransformableMethod implements TransformableMethod {
	private final TransformableMethod wrapped;
	private final CodeSlicer slicer;

	private Optional<SlicedTransformableCode> code;

	public SlicedTransformableMethod(TransformableMethod wrapped, CodeSlicer slicer) {
		this.wrapped = wrapped;
		this.slicer = slicer;
	}

	@Override
	public MethodModel model() {
		return this.wrapped.model();
	}

	@Override
	public TransformableClass owner() {
		return this.wrapped.owner();
	}

	@Override
	public Optional<SlicedTransformableCode> code() {
		if (this.code == null) {
			this.code = this.wrapped.code().map(this.slicer::slice);
		}

		return this.code;
	}

	@Override
	public void transform(MethodTransform transform) {
		this.wrapped.transform(transform);
	}

	@Override
	public String toString() {
		return "(sliced) " + this.wrapped.toString();
	}
}
