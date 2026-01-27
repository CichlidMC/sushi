package fish.cichlidmc.sushi.api.transformer.builtin.access;

import fish.cichlidmc.sushi.api.attach.AttachmentKey;
import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.metadata.PublicizedBy;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/// Changes the access of a class to public. Fails if it's already public.
public record PublicizeClassTransformer(ClassPredicate classPredicate) implements SimpleTransformer {
	public static final MapCodec<PublicizeClassTransformer> CODEC = ClassPredicate.CODEC.xmap(
			PublicizeClassTransformer::new, PublicizeClassTransformer::classPredicate
	).fieldOf("class");

	private static final AttachmentKey<Marker> markerKey = new AttachmentKey<>();

	@Override
	public void apply(TransformContext context) throws TransformException {
		TransformableClass target = context.target();

		// we want to error if this transformer doesn't do anything, but it's possible
		// the class was made public in a previous phase, which is perfectly fine.
		if (target.model().flags().flags().contains(AccessFlag.PUBLIC) && !target.attachments().has(markerKey)) {
			throw new TransformException("Class is already public");
		}

		target.transformDirect(_ -> (builder, element) -> {
			if (element instanceof AccessFlags flags) {
				builder.withFlags(publicize(flags));
				return;
			} else if (element instanceof InnerClassesAttribute innerClasses) {
				if (innerClasses.classes().stream().anyMatch(info -> info.innerClass().asSymbol().equals(target.desc()))) {
					List<InnerClassInfo> infos = new ArrayList<>();
					for (InnerClassInfo innerClass : innerClasses.classes()) {
						if (!innerClass.innerClass().asSymbol().equals(target.desc())) {
							infos.add(innerClass);
							continue;
						}

						Set<AccessFlag> flags = EnumSet.copyOf(innerClass.flags());
						publicize(flags);

						infos.add(InnerClassInfo.of(
								target.desc(),
								innerClass.outerClass().map(ClassEntry::asSymbol),
								innerClass.innerName().map(Utf8Entry::stringValue),
								flags.toArray(AccessFlag[]::new)
						));
					}
					builder.with(InnerClassesAttribute.of(infos));
					return;
				}
			}

			builder.with(element);
		});

		if (context.addMetadata()) {
			Consumer<Annotations> modifier = addMetadata(context.transformerId());
			target.transformDirect(_ -> Annotations.runtimeVisibleClassModifier(modifier));
		}

		target.attachments().set(markerKey, Marker.INSTANCE);
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC;
	}

	static AccessFlag[] publicize(AccessFlags flags) {
		if (flags.flags().contains(AccessFlag.PUBLIC)) {
			return flags.flags().toArray(AccessFlag[]::new);
		}

		Set<AccessFlag> set = EnumSet.copyOf(flags.flags());
		publicize(set);
		return set.toArray(AccessFlag[]::new);
	}

	static void publicize(Set<AccessFlag> flags) {
		flags.remove(AccessFlag.PRIVATE);
		flags.remove(AccessFlag.PROTECTED);
		flags.add(AccessFlag.PUBLIC);
	}
	
	static Consumer<Annotations> addMetadata(Id id) {
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

	private enum Marker {
		INSTANCE
	}
}
