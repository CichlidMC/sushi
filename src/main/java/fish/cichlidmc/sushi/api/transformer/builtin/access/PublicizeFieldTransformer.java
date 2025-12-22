package fish.cichlidmc.sushi.api.transformer.builtin.access;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.FieldTarget;
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

import static fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer.addMetadata;
import static fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer.publicize;

/// Changes a field's access to public. Fails if it's already public.
public record PublicizeFieldTransformer(ClassTarget classes, FieldTarget field) implements SimpleTransformer {
	public static final DualCodec<PublicizeFieldTransformer> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("classes"), PublicizeFieldTransformer::classes,
			FieldTarget.CODEC.fieldOf("field"), PublicizeFieldTransformer::field,
			PublicizeFieldTransformer::new
	);

	@Override
	public void apply(TransformContext context) throws TransformException {
		TransformableField field = this.field.findSingle(context.clazz()).orElse(null);
		if (field == null)
			return;

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

			Id id = context.transformerId();
			field.transform(Annotations.runtimeVisibleFieldModifier(addMetadata(id)));
		});
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
