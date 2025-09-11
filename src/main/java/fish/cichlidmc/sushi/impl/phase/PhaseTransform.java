package fish.cichlidmc.sushi.impl.phase;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
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
			this.context.setCurrentId(transformer.id());
			transformer.transform().apply(this.context);
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
				TransformableMethodImpl impl = (TransformableMethodImpl) method;
				impl.toTransform(methodGenerator).ifPresentOrElse(
						transform -> builder.transformMethod(method.model(), transform),
						() -> builder.with(method.model())
				);
			}

			for (TransformableField field : clazz.fields()) {
				TransformableFieldImpl impl = (TransformableFieldImpl) field;
				impl.transform().ifPresentOrElse(
						transform -> builder.transformField(field.model(), transform),
						() -> builder.with(field.model())
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
