package fish.cichlidmc.sushi.impl.transform.access;

import fish.cichlidmc.sushi.api.target.FieldTarget;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.AccessFlags;

import static fish.cichlidmc.sushi.impl.transform.access.PublicizeClassTransform.addMetadata;
import static fish.cichlidmc.sushi.impl.transform.access.PublicizeClassTransform.publicize;

public record PublicizeFieldTransform(FieldTarget field) implements Transform {
	public static final MapCodec<PublicizeFieldTransform> CODEC = FieldTarget.CODEC.xmap(
			PublicizeFieldTransform::new, PublicizeFieldTransform::field
	).fieldOf("field");

	@Override
	public void apply(TransformContext context) throws TransformException {
		this.field.findSingle(context.clazz()).ifPresent(field -> {
			if (field.model().flags().flags().contains(AccessFlag.PUBLIC)) {
				throw new TransformException("Field is already public");
			}

			field.transform((builder, element) -> builder.with(
					element instanceof AccessFlags flags ? publicize(flags, AccessFlags::ofField) : element
			));

			if (!context.addMetadata())
				return;

			Id id = context.transformerId();
			field.transform(Annotations.runtimeVisibleFieldModifier(addMetadata(id)));
		});
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}
}
