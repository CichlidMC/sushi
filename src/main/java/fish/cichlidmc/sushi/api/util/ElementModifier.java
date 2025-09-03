package fish.cichlidmc.sushi.api.util;

import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassFileBuilder;
import org.glavo.classfile.ClassFileElement;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.FieldBuilder;
import org.glavo.classfile.FieldElement;
import org.glavo.classfile.FieldTransform;
import org.glavo.classfile.MethodBuilder;
import org.glavo.classfile.MethodElement;
import org.glavo.classfile.MethodTransform;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility for modifying (or creating, if it doesn't exist) a {@link ClassFileElement}.
 * Consists of a supplier which will be invoked if the element is not found,
 * and a function that modifies the found (or created) element.
 */
public final class ElementModifier<E extends ClassFileElement, T extends E, B extends ClassFileBuilder<E, B>> {
	private final Class<T> type;
	private final Supplier<T> creator;
	private final BiFunction<B, T, T> modifier;

	private T found;

	private ElementModifier(Class<T> type, Supplier<T> creator, BiFunction<B, T, T> modifier) {
		this.type = type;
		this.creator = creator;
		this.modifier = modifier;
	}

	@SuppressWarnings("unchecked")
	private void accept(B builder, E element) {
		if (this.type.isInstance(element)) {
			this.found = (T) element;
		} else {
			builder.with(element);
		}
	}

	private void atEnd(B builder) {
		T value = Objects.requireNonNullElseGet(this.found, this.creator);
		T modified = this.modifier.apply(builder, value);
		builder.with(modified);
	}

	public static <T extends ClassElement> ClassTransform forClass(Class<T> type, Supplier<T> creator, BiFunction<ClassBuilder, T, T> function) {
		ElementModifier<ClassElement, T, ClassBuilder> modifier = new ElementModifier<>(type, creator, function);

		return new ClassTransform() {
			@Override
			public void accept(ClassBuilder builder, ClassElement element) {
				modifier.accept(builder, element);
			}

			@Override
			public void atEnd(ClassBuilder builder) {
				modifier.atEnd(builder);
			}
		};
	}

	public static <T extends MethodElement> MethodTransform forMethod(Class<T> type, Supplier<T> creator, BiFunction<MethodBuilder, T, T> function) {
		ElementModifier<MethodElement, T, MethodBuilder> modifier = new ElementModifier<>(type, creator, function);

		return new MethodTransform() {
			@Override
			public void accept(MethodBuilder builder, MethodElement element) {
				modifier.accept(builder, element);
			}

			@Override
			public void atEnd(MethodBuilder builder) {
				modifier.atEnd(builder);
			}
		};
	}

	public static <T extends FieldElement> FieldTransform forField(Class<T> type, Supplier<T> creator, BiFunction<FieldBuilder, T, T> function) {
		ElementModifier<FieldElement, T, FieldBuilder> modifier = new ElementModifier<>(type, creator, function);

		return new FieldTransform() {
			@Override
			public void accept(FieldBuilder builder, FieldElement element) {
				modifier.accept(builder, element);
			}

			@Override
			public void atEnd(FieldBuilder builder) {
				modifier.atEnd(builder);
			}
		};
	}

	public static <T extends CodeElement> CodeTransform forCode(Class<T> type, Supplier<T> creator, BiFunction<CodeBuilder, T, T> function) {
		ElementModifier<CodeElement, T, CodeBuilder> modifier = new ElementModifier<>(type, creator, function);

		return new CodeTransform() {
			@Override
			public void accept(CodeBuilder builder, CodeElement element) {
				modifier.accept(builder, element);
			}

			@Override
			public void atEnd(CodeBuilder builder) {
				modifier.atEnd(builder);
			}
		};
	}
}
