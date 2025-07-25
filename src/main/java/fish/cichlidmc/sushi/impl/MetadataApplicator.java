package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.SushiMetadata;
import fish.cichlidmc.sushi.api.Transformer;
import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public final class MetadataApplicator implements ClassTransform {
	public static final ClassDesc METADATA_DESC = SushiMetadata.class.describeConstable().orElseThrow();

	private final List<Transformer> transformers;

	private RuntimeVisibleAnnotationsAttribute classAnnotations;

	public MetadataApplicator(List<Transformer> transformers) {
		this.transformers = transformers;
	}

	@Override
	public void accept(ClassBuilder builder, ClassElement element) {
		if (element instanceof RuntimeVisibleAnnotationsAttribute annotations) {
			this.classAnnotations = annotations;
		}
	}

	@Override
	public void atEnd(ClassBuilder builder) {
		AnnotationValue[] lines = this.transformers.stream()
				.map(transformer -> transformer.id + " - " + transformer.describe())
				.map(AnnotationValue::ofString)
				.toArray(AnnotationValue[]::new);

		List<Annotation> annotations = new ArrayList<>();
		if (this.classAnnotations != null) {
			annotations.addAll(this.classAnnotations.annotations());
		}

		// put metadata at the top
		annotations.addFirst(Annotation.of(METADATA_DESC, AnnotationElement.ofArray("value", lines)));

		builder.with(RuntimeVisibleAnnotationsAttribute.of(annotations));
	}
}
