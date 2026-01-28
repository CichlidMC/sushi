package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.detail.Detail;
import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.util.List;
import java.util.Optional;

public final class SingleStepTransform implements ClassTransform {
	private final TransformableClassImpl clazz;
	private final List<TransformStep> steps;
	private final TransformStep step;

	public SingleStepTransform(TransformableClassImpl clazz, List<TransformStep> steps, int index) {
		this.clazz = clazz;
		this.steps = steps;
		this.step = steps.get(index);
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
	}

	@Override
	public void atStart(ClassBuilder originalBuilder) {
		// register all changes transformers want to make
		for (PreparedTransform transform : this.step.transforms()) {
			TransformContextImpl context = new TransformContextImpl(this.clazz, transform);

			Detail.Provider detail = Detail.Provider.of(() -> this.createDetail(transform));
			ScopedValue.where(TransformContextImpl.CURRENT, context).run(() -> Details.with(
					"Transformers", detail, TransformException::new,
					() -> transform.transform.apply(context)
			));
		}

		// no errors thrown, apply
		this.clazz.freeze();
		originalBuilder.transform(this.clazz.model(), this.clazz.append(new ActualTransform()));
	}

	// format:     phase      transformers  barrier   comma-separated   -> current <-
	//			mymod:early[mymod:a, mymod:b] | default[mymod:c], late[-> mymod:d <-]
	private String createDetail(PreparedTransform current) {
		boolean foundThisStep = false;
		boolean foundCurrent = false;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < this.steps.size(); i++) {
			TransformStep step = this.steps.get(i);

			foundThisStep |= step == this.step;
			foundCurrent |= step.contains(current);

			if (i != 0) {
				builder.append(" | ");
			}

			builder.append(step.toDetail(current));
		}

		if (!foundThisStep) {
			throw new IllegalStateException("This step is missing from the steps list?");
		} else if (!foundCurrent) {
			throw new IllegalStateException("Did not find current transform");
		}

		return builder.toString();
	}

	// this is done in a separate transform so direct transforms can be applied to the results.
	private class ActualTransform implements ClassTransform {
		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
		}

		@Override
		public void atStart(ClassBuilder builder) {
			for (TransformableMethod method : SingleStepTransform.this.clazz.methods().values()) {
				MethodModel model = method.model();
				TransformableMethodImpl impl = (TransformableMethodImpl) method;

				Details.with("Method", method.key(), TransformException::new, () -> {
					Optional<MethodTransform> transform = impl.toTransform(builder);
					transform.ifPresentOrElse(
							t -> builder.transformMethod(model, t),
							() -> builder.with(model)
					);
				});
			}

			for (TransformableField field : SingleStepTransform.this.clazz.fields().values()) {
				FieldModel model = field.model();
				TransformableFieldImpl impl = (TransformableFieldImpl) field;

				Details.with("Field", field.key(), TransformException::new, () -> {
					Optional<FieldTransform> transform = impl.toTransform(builder);
					transform.ifPresentOrElse(
							t -> builder.transformField(model, t),
							() -> builder.with(model)
					);
				});
			}

			for (ClassElement element : SingleStepTransform.this.clazz.model()) {
				if (!(element instanceof MethodModel) && !(element instanceof FieldModel)) {
					builder.with(element);
				}
			}
		}
	}
}
