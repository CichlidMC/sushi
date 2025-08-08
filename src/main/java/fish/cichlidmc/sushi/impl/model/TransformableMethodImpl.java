package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.apply.MethodGenerator;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodTransform;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class TransformableMethodImpl implements TransformableMethod {
	private final MethodModel model;
	private final TransformableClassImpl owner;

	@Nullable
	private Optional<TransformableCodeImpl> code;
	private MethodTransform rawTransform;

	public TransformableMethodImpl(MethodModel model, TransformableClassImpl owner) {
		this.model = model;
		this.owner = owner;
	}

	@Override
	public MethodModel model() {
		return this.model;
	}

	@Override
	public TransformableClassImpl owner() {
		return this.owner;
	}

	@Override
	public Optional<TransformableCodeImpl> code() {
		if (this.code == null) {
			this.code = this.model.code().map(code -> new TransformableCodeImpl(code, this));
		}

		return this.code;
	}

	@Override
	public void transform(MethodTransform transform) {
		if (this.rawTransform == null) {
			this.rawTransform = transform;
		} else {
			this.rawTransform = this.rawTransform.andThen(transform);
		}
	}

	public Optional<MethodTransform> toTransform(MethodGenerator methodGenerator) {
		if (this.code == null || this.code.isEmpty())
			return this.fallback();

		TransformableCodeImpl code = this.code.get();
		Optional<CodeTransform> applicator = code.operations.applicator(code.model().elements(), methodGenerator);
		if (applicator.isEmpty()) {
			return this.fallback();
		}

		MethodTransform transform = MethodTransform.transformingCode(applicator.get());
		return Optional.of(this.rawTransform == null ? transform : transform.andThen(this.rawTransform));
	}

	private Optional<MethodTransform> fallback() {
		return Optional.ofNullable(this.rawTransform);
	}
}
