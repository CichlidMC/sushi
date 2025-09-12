package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.FieldTransform;
import org.glavo.classfile.MethodTransform;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Mutable annotation manager, because modifying them directly is miserable.
 */
public final class Annotations {
	private final List<Entry> entries;

	private Annotations(List<Entry> entries) {
		this.entries = new ArrayList<>(entries);
	}

	public Entry findOrCreate(Predicate<Entry> predicate, Supplier<Entry> supplier) {
		for (Entry entry : this.entries) {
			if (predicate.test(entry)) {
				return entry;
			}
		}

		Entry entry = supplier.get();
		this.entries.add(entry);
		return entry;
	}

	public void addFirst(Entry entry) {
		this.entries.addFirst(entry);
	}

	public void addLast(Entry entry) {
		this.entries.addLast(entry);
	}

	public RuntimeVisibleAnnotationsAttribute toRuntimeVisibleAttribute() {
		return RuntimeVisibleAnnotationsAttribute.of(this.entries.stream().map(Entry::toAnnotation).toList());
	}

	public static Annotations of(RuntimeVisibleAnnotationsAttribute attribute) {
		return new Annotations(attribute.annotations().stream().map(Entry::new).toList());
	}

	public static ClassTransform runtimeVisibleClassModifier(Consumer<Annotations> consumer) {
		return ElementModifier.forClass(RuntimeVisibleAnnotationsAttribute.class, RuntimeVisibleAnnotationsAttribute::of, (builder, attribute) -> {
			Annotations annotations = Annotations.of(attribute);
			consumer.accept(annotations);
			return annotations.toRuntimeVisibleAttribute();
		});
	}

	public static MethodTransform runtimeVisibleMethodModifier(Consumer<Annotations> consumer) {
		return ElementModifier.forMethod(RuntimeVisibleAnnotationsAttribute.class, RuntimeVisibleAnnotationsAttribute::of, (builder, attribute) -> {
			Annotations annotations = Annotations.of(attribute);
			consumer.accept(annotations);
			return annotations.toRuntimeVisibleAttribute();
		});
	}

	public static FieldTransform runtimeVisibleFieldModifier(Consumer<Annotations> consumer) {
		return ElementModifier.forField(RuntimeVisibleAnnotationsAttribute.class, RuntimeVisibleAnnotationsAttribute::of, (builder, attribute) -> {
			Annotations annotations = Annotations.of(attribute);
			consumer.accept(annotations);
			return annotations.toRuntimeVisibleAttribute();
		});
	}

	public static final class Entry {
		public final ClassDesc desc;
		private final Map<String, AnnotationValue> fields;

		public Entry(Class<? extends java.lang.annotation.Annotation> clazz) {
			this(ClassDescs.of(clazz));
		}

		public Entry(ClassDesc desc) {
			this.desc = desc;
			this.fields = new HashMap<>();
		}

		public Entry(Annotation annotation) {
			this.desc = annotation.classSymbol();
			this.fields = new HashMap<>();
			for (AnnotationElement element : annotation.elements()) {
				this.fields.put(element.name().stringValue(), element.value());
			}
		}

		public Optional<AnnotationValue> get(String field) {
			return Optional.ofNullable(this.fields.get(field));
		}

		public Entry put(String field, AnnotationValue value) {
			this.fields.put(field, value);
			return this;
		}

		public Annotation toAnnotation() {
			List<AnnotationElement> elements = new ArrayList<>();
			this.fields.forEach((name, value) -> elements.add(AnnotationElement.of(name, value)));
			return Annotation.of(this.desc, elements);
		}
	}
}
