package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.impl.MethodEntry;
import fish.cichlidmc.sushi.impl.SelectionApplicator;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionImpl;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodTransform;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

	public Optional<MethodTransform> toTransform(Consumer<MethodEntry> methodGenerator) {
		if (this.code == null || this.code.isEmpty())
			return this.fallback();

		TransformableCodeImpl code = this.code.get();
		List<SelectionImpl> selections = code.selections().selections;
		if (selections.isEmpty()) {
			return this.fallback();
		}

		MethodTransform transform = MethodTransform.transformingCode(
				SelectionApplicator.create(code.model().elements(), selections, methodGenerator)
		);
		return Optional.of(this.rawTransform == null ? transform : transform.andThen(this.rawTransform));
	}

	private Optional<MethodTransform> fallback() {
		return Optional.ofNullable(this.rawTransform);
	}
}
