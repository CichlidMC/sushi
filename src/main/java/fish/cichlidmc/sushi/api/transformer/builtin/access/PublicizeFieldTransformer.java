package fish.cichlidmc.sushi.api.transformer.builtin.access;

import fish.cichlidmc.sushi.api.attach.AttachmentKey;
import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.field.FieldTarget;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.AccessFlags;
import java.lang.reflect.AccessFlag;
import java.util.Collection;
import java.util.function.Consumer;

import static fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer.addMetadata;
import static fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer.publicize;

/// Changes a field's access to public. Fails if it's already public.
public record PublicizeFieldTransformer(ClassPredicate classPredicate, FieldTarget field) implements SimpleTransformer {
	public static final DualCodec<PublicizeFieldTransformer> CODEC = CompositeCodec.of(
			ClassPredicate.CODEC.fieldOf("class"), PublicizeFieldTransformer::classPredicate,
			FieldTarget.CODEC.fieldOf("field"), PublicizeFieldTransformer::field,
			PublicizeFieldTransformer::new
	);

	// use a marker attachment to indicate that a field has been publicized.
	// if present, we do nothing, since a different transformer has already done the work.
	private static final AttachmentKey<Marker> markerKey = new AttachmentKey<>();

	@Override
	public void apply(TransformContext context) throws TransformException {
		Collection<TransformableField> found = this.field.find(context.target());

		for (TransformableField field : found) {
			Details.with("Field", field, TransformException::new, () -> {
				if (!field.attachments().has(markerKey)) {
					// only publicize if nothing else has done it yet
					if (field.model().flags().flags().contains(AccessFlag.PUBLIC)) {
						// we want to throw an exception when this transformer is useless.
						// this won't catch transformers from previous phases due to the marker.
						throw new TransformException("Field is already public");
					}

					field.transform((builder, element) -> {
						if (element instanceof AccessFlags flags) {
							builder.withFlags(publicize(flags));
						} else {
							builder.with(element);
						}
					});

					field.attachments().set(markerKey, Marker.INSTANCE);
				}

				// add a metadata annotation to make it clear what transformer(s) publicized the field.
				// we do this regardless of the marker, since this transformer *would've* publicized it
				// if the marker wasn't present.
				if (context.addMetadata()) {
					Consumer<Annotations> modifier = addMetadata(context.transformerId());
					field.transform(Annotations.runtimeVisibleFieldModifier(modifier));
				}
			});
		}
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}

	private enum Marker {
		INSTANCE
	}
}
