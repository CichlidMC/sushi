package fish.cichlidmc.sushi.api.transformer.builtin.access;

import fish.cichlidmc.sushi.api.metadata.PublicizedBy;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
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
public record PublicizeClassTransformer(ClassTarget classes) implements SimpleTransformer {
	public static final MapCodec<PublicizeClassTransformer> CODEC = ClassTarget.CODEC.xmap(
			PublicizeClassTransformer::new, PublicizeClassTransformer::classes
	).fieldOf("target");

	@Override
	public void apply(TransformContext context) throws TransformException {
		TransformableClass clazz = context.clazz();

		if (clazz.model().flags().flags().contains(AccessFlag.PUBLIC)) {
			// we want to error if this transformer doesn't do anything, but it's possible
			// the class was made public in a previous phase, which is perfectly fine.
			boolean publicized = clazz.model().findAttribute(Attributes.runtimeVisibleAnnotations()).map(attribute -> {
				for (Annotation annotation : attribute.annotations()) {
					if (ClassDescs.equals(annotation.classSymbol(), PublicizedBy.class)) {
						return true;
					}
				}

				return false;
			}).orElse(false);

			if (!publicized) {
				throw new TransformException("Class is already public");
			}
		}

		clazz.transform((builder, element) -> {
			if (element instanceof AccessFlags flags) {
				builder.withFlags(publicize(flags));
				return;
			} else if (element instanceof InnerClassesAttribute innerClasses) {
				if (innerClasses.classes().stream().anyMatch(info -> info.innerClass().asSymbol().equals(clazz.desc()))) {
					List<InnerClassInfo> infos = new ArrayList<>();
					for (InnerClassInfo innerClass : innerClasses.classes()) {
						if (!innerClass.innerClass().asSymbol().equals(clazz.desc())) {
							infos.add(innerClass);
							continue;
						}

						Set<AccessFlag> flags = EnumSet.copyOf(innerClass.flags());
						publicize(flags);

						infos.add(InnerClassInfo.of(
								clazz.desc(),
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

		if (!context.addMetadata())
			return;

		Consumer<Annotations> modifier = addMetadata(context.transformerId());
		clazz.transform(Annotations.runtimeVisibleClassModifier(modifier));
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
}
