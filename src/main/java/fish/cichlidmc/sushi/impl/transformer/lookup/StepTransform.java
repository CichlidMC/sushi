package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.detail.Detail;
import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
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
import java.util.SequencedSet;

public final class StepTransform implements ClassTransform {
	private final SequencedSet<PreparedTransform> transforms;
	private final TransformContextImpl context;

	public StepTransform(SequencedSet<PreparedTransform> transforms, TransformContextImpl context) {
		this.transforms = transforms;
		this.context = context;
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
	}

	@Override
	public void atStart(ClassBuilder originalBuilder) {
		// register all changes transformers want to make
		for (PreparedTransform transform : this.transforms) {
			Id id = transform.owner.id();
			this.context.setCurrentId(id);

			Details.with(
					"Current Transformer", id, TransformException::new,
					() -> transform.transform.apply(this.context)
			);
		}

		this.context.finish();

		// no errors thrown, apply
		TransformableClassImpl clazz = this.context.clazz();
		originalBuilder.transform(clazz.model(), clazz.append(new ActualTransform()));
	}

	// this is done in a separate transform so the rawTransform can be applied to the results.
	private class ActualTransform implements ClassTransform {
		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
		}

		@Override
		public void atStart(ClassBuilder builder) {
			TransformableClassImpl clazz = StepTransform.this.context.clazz();
			MethodGenerator methodGenerator = MethodGenerator.of(builder);

			for (TransformableMethod method : clazz.methods()) {
				MethodModel model = method.model();
				TransformableMethodImpl impl = (TransformableMethodImpl) method;

				Detail.Provider detail = Detail.Provider.of(
						() -> model.methodName().stringValue() + model.methodType().stringValue()
				);

				Details.with(
						"Method", detail, TransformException::new,
						() -> impl.toTransform(methodGenerator).ifPresentOrElse(
								transform -> builder.transformMethod(model, transform),
								() -> builder.with(model)
						)
				);
			}

			for (TransformableField field : clazz.fields()) {
				FieldModel model = field.model();
				TransformableFieldImpl impl = (TransformableFieldImpl) field;

				Detail.Provider detail = Detail.Provider.of(
						() -> ClassDescs.fullName(model.fieldTypeSymbol()) + ' ' + model.fieldName().stringValue()
				);

				Details.with(
						"Field", detail, TransformException::new,
						() -> impl.transform().ifPresentOrElse(
								transform -> builder.transformField(model, transform),
								() -> builder.with(model)
						)
				);
			}

			for (ClassElement element : clazz.model()) {
				if (!(element instanceof MethodModel) && !(element instanceof FieldModel)) {
					builder.with(element);
				}
			}
		}
	}
}
