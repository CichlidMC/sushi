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
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.AccessFlags;
import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.Attributes;
import org.glavo.classfile.attribute.InnerClassInfo;
import org.glavo.classfile.attribute.InnerClassesAttribute;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.constantpool.Utf8Entry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Changes the access of a class to public. Fails if it's already public.
 */
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
			boolean publicized = clazz.model().findAttribute(Attributes.RUNTIME_VISIBLE_ANNOTATIONS).map(attribute -> {
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
				builder.with(publicize(flags, AccessFlags::ofClass));
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

		Id id = context.transformerId();
		clazz.transform(Annotations.runtimeVisibleClassModifier(addMetadata(id)));
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC;
	}

	static AccessFlags publicize(AccessFlags flags, Function<AccessFlag[], AccessFlags> factory) {
		if (flags.flags().contains(AccessFlag.PUBLIC))
			return flags;

		Set<AccessFlag> set = EnumSet.copyOf(flags.flags());
		publicize(set);
		AccessFlag[] array = set.toArray(AccessFlag[]::new);
		return factory.apply(array);
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
