package fish.cichlidmc.sushi.impl.phase;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.FieldModel;
import org.glavo.classfile.MethodModel;

import java.util.List;

public final class PhaseTransform implements ClassTransform {
	private final List<Transformer> transformers;
	private final TransformContextImpl context;

	public PhaseTransform(List<Transformer> transformers, TransformContextImpl context) {
		this.transformers = transformers;
		this.context = context;
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
	}

	@Override
	public void atStart(ClassBuilder originalBuilder) {
		// register all changes transformers want to make
		for (Transformer transformer : this.transformers) {
			Id id = transformer.id();
			this.context.setCurrentId(id);

			TransformException.withDetail(
					"Current Transformer", id,
					() -> transformer.transform().apply(this.context)
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
			TransformableClassImpl clazz = PhaseTransform.this.context.clazz();
			MethodGenerator methodGenerator = MethodGenerator.of(builder);

			for (TransformableMethod method : clazz.methods()) {
				MethodModel model = method.model();
				TransformableMethodImpl impl = (TransformableMethodImpl) method;

				String detail = model.methodName().stringValue() + model.methodType().stringValue();
				TransformException.withDetail(
						"Method", detail,
						() -> impl.toTransform(methodGenerator).ifPresentOrElse(
								transform -> builder.transformMethod(model, transform),
								() -> builder.with(model)
						)
				);
			}

			for (TransformableField field : clazz.fields()) {
				FieldModel model = field.model();
				TransformableFieldImpl impl = (TransformableFieldImpl) field;

				String detail = ClassDescs.fullName(model.fieldTypeSymbol()) + ' ' + model.fieldName().stringValue();
				TransformException.withDetail(
						"Field", detail,
						() -> impl.transform().ifPresentOrElse(
								transform -> builder.transformField(model, transform),
								() -> builder.with(model)
						)
				);
			}

			clazz.model().forEachElement(element -> {
				if (!(element instanceof MethodModel) && !(element instanceof FieldModel)) {
					builder.with(element);
				}
			});
		}
	}
}
