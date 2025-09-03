package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	public RuntimeVisibleAnnotationsAttribute toRuntimeVisibleAttribute() {
		return RuntimeVisibleAnnotationsAttribute.of(this.entries.stream().map(Entry::toAnnotation).toList());
	}

	public static Annotations of(RuntimeVisibleAnnotationsAttribute attribute) {
		return new Annotations(attribute.annotations().stream().map(Entry::new).toList());
	}

	public static final class Entry {
		public final ClassDesc desc;
		private final Map<String, AnnotationValue> fields;

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
