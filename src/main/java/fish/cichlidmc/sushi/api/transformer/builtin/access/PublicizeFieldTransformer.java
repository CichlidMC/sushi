package fish.cichlidmc.sushi.api.transformer.builtin.access;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.FieldTarget;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
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

	@Override
	public void apply(TransformContext context) throws TransformException {
		TransformableField field = this.field.findSingleOrThrow(context.target());

		Details.with("Field", field, TransformException::new, () -> {
			if (field.model().flags().flags().contains(AccessFlag.PUBLIC)) {
				throw new TransformException("Field is already public");
			}

			field.transform((builder, element) -> {
				if (element instanceof AccessFlags flags) {
					builder.withFlags(publicize(flags));
				} else {
					builder.with(element);
				}
			});

			if (!context.addMetadata())
				return;

			Consumer<Annotations> modifier = addMetadata(context.transformerId());
			field.transform(Annotations.runtimeVisibleFieldModifier(modifier));
		});
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
