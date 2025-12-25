package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;

public final class SingleStepTransform implements ClassTransform {
	private final TransformableClassImpl clazz;
	private final TransformStep step;

	public SingleStepTransform(TransformableClassImpl clazz, TransformStep step) {
		this.clazz = clazz;
		this.step = step;
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
	}

	@Override
	public void atStart(ClassBuilder originalBuilder) {
		// register all changes transformers want to make
		for (PreparedTransform transform : this.step.transforms()) {
			TransformContextImpl context = new TransformContextImpl(this.clazz, transform);

			ScopedValue.where(TransformContextImpl.CURRENT, context).run(() -> Details.with(
					"Current Transformer", transform.owner.id(), TransformException::new,
					() -> transform.transform.apply(context)
			));
		}

		// no errors thrown, apply
		originalBuilder.transform(this.clazz.model(), this.clazz.append(new ActualTransform()));
	}

	// this is done in a separate transform so the rawTransform can be applied to the results.
	private class ActualTransform implements ClassTransform {
		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
		}

		@Override
		public void atStart(ClassBuilder builder) {
			MethodGenerator methodGenerator = MethodGenerator.of(builder);

			for (TransformableMethod method : SingleStepTransform.this.clazz.methods().values()) {
				MethodModel model = method.model();
				TransformableMethodImpl impl = (TransformableMethodImpl) method;

				Details.with(
						"Method", method.key(), TransformException::new,
						() -> impl.toTransform(methodGenerator).ifPresentOrElse(
								transform -> builder.transformMethod(model, transform),
								() -> builder.with(model)
						)
				);
			}

			for (TransformableField field : SingleStepTransform.this.clazz.fields().values()) {
				FieldModel model = field.model();
				TransformableFieldImpl impl = (TransformableFieldImpl) field;

				Details.with(
						"Field", field.key(), TransformException::new,
						() -> impl.transform().ifPresentOrElse(
								transform -> builder.transformField(model, transform),
								() -> builder.with(model)
						)
				);
			}

			for (ClassElement element : SingleStepTransform.this.clazz.model()) {
				if (!(element instanceof MethodModel) && !(element instanceof FieldModel)) {
					builder.with(element);
				}
			}
		}
	}
}
