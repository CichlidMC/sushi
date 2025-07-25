package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;
import fish.cichlidmc.sushi.impl.model.TransformableFieldImpl;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.FieldModel;
import org.glavo.classfile.MethodModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ManagedTransform implements ClassTransform {
	private final List<Transformer> transformers;
	private final TransformContextImpl context;

	public ManagedTransform(List<Transformer> transformers, TransformContextImpl context) {
		this.transformers = transformers;
		this.context = context;
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
	}

	@Override
	public void atStart(ClassBuilder originalBuilder) {
		for (Transformer transformer : this.transformers) {
			this.context.setCurrentId(transformer.id);
			transformer.transform.apply(this.context);
		}

		TransformableClassImpl clazz = this.context.clazz();

		clazz.methods().stream()
				.map(TransformableMethodImpl::code)
				.flatMap(Optional::stream)
				.map(TransformableCodeImpl::selections)
				.forEach(SelectionBuilderImpl::checkForConflicts);

		originalBuilder.transform(clazz.model(), clazz.append(new MainTransform()));
	}

	private class MainTransform implements ClassTransform {
		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
		}

		@Override
		public void atStart(ClassBuilder builder) {
			TransformableClassImpl clazz = ManagedTransform.this.context.clazz();

			List<MethodEntry> newMethods = new ArrayList<>();

			for (TransformableMethodImpl method : clazz.methods()) {
				method.toTransform(newMethods::add).ifPresentOrElse(
						transform -> builder.transformMethod(method.model(), transform),
						() -> builder.with(method.model())
				);
			}

			for (MethodEntry entry : newMethods) {
				builder.withMethod(entry.name(), entry.desc(), entry.flags(), entry.consumer());
			}

			for (TransformableFieldImpl field : clazz.fields()) {
				field.transform().ifPresentOrElse(
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
