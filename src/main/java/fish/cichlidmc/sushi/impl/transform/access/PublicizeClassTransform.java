package fish.cichlidmc.sushi.impl.transform.access;

import fish.cichlidmc.sushi.api.metadata.PublicizedBy;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.AccessFlags;
import org.glavo.classfile.AnnotationValue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public enum PublicizeClassTransform implements Transform {
	INSTANCE;

	public static final MapCodec<PublicizeClassTransform> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public void apply(TransformContext context) throws TransformException {
		TransformableClass clazz = context.clazz();
		if (clazz.model().flags().flags().contains(AccessFlag.PUBLIC)) {
			throw new TransformException("Class is already public");
		}

		clazz.transform((builder, element) -> builder.with(
				element instanceof AccessFlags flags ? publicize(flags, AccessFlags::ofClass) : element
		));

		if (!context.addMetadata())
			return;

		Id id = context.transformerId();
		clazz.transform(Annotations.runtimeVisibleClassModifier(addMetadata(id)));
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return CODEC;
	}

	public static AccessFlags publicize(AccessFlags flags, Function<AccessFlag[], AccessFlags> factory) {
		if (flags.flags().contains(AccessFlag.PUBLIC))
			return flags;

		Set<AccessFlag> set = EnumSet.copyOf(flags.flags());
		set.remove(AccessFlag.PRIVATE);
		set.remove(AccessFlag.PROTECTED);
		set.add(AccessFlag.PUBLIC);
		AccessFlag[] array = set.toArray(AccessFlag[]::new);
		return factory.apply(array);
	}
	
	public static Consumer<Annotations> addMetadata(Id id) {
		return annotations -> {
			Annotations.Entry entry = annotations.findOrCreate(
					e -> ClassDescs.equals(e.desc, PublicizedBy.class),
					() -> new Annotations.Entry(PublicizedBy.class)
			);

			List<AnnotationValue> ids = new ArrayList<>(
					entry.get("value")
							.filter(value -> value instanceof AnnotationValue.OfArray)
							.map(value -> (AnnotationValue.OfArray) value)
							.map(AnnotationValue.OfArray::values)
							.orElse(List.of())
			);

			ids.add(AnnotationValue.ofString(id.toString()));
			entry.put("value", AnnotationValue.ofArray(ids));
		};
	}
}
