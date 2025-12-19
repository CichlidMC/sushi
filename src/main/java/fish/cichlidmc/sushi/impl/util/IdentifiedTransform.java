package fish.cichlidmc.sushi.impl.util;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.TransformException;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodTransform;

// not being able to implement ClassFileTransform is so annoying
public abstract class IdentifiedTransform<T> {
	public static final String DETAIL_NAME = "Direct Transformer from";

	private final Id owner;
	protected final T wrapped;

	private IdentifiedTransform(Id owner, T wrapped) {
		this.owner = owner;
		this.wrapped = wrapped;
	}

	protected void withDetail(Runnable runnable) {
		TransformException.withDetail(DETAIL_NAME, this.owner, runnable);
	}

	public static final class Class extends IdentifiedTransform<ClassTransform> implements ClassTransform {
		public Class(Id owner, ClassTransform wrapped) {
			super(owner, wrapped);
		}

		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
			this.withDetail(() -> this.wrapped.accept(builder, element));
		}

		@Override
		public void atStart(ClassBuilder builder) {
			this.withDetail(() -> this.wrapped.atStart(builder));
		}

		@Override
		public void atEnd(ClassBuilder builder) {
			this.withDetail(() -> this.wrapped.atEnd(builder));
		}
	}

	public static final class Method extends IdentifiedTransform<MethodTransform> implements MethodTransform {
		public Method(Id owner, MethodTransform wrapped) {
			super(owner, wrapped);
		}

		@Override
		public void accept(MethodBuilder builder, MethodElement element) {
			this.withDetail(() -> this.wrapped.accept(builder, element));
		}

		@Override
		public void atStart(MethodBuilder builder) {
			this.withDetail(() -> this.wrapped.atStart(builder));
		}

		@Override
		public void atEnd(MethodBuilder builder) {
			this.withDetail(() -> this.wrapped.atEnd(builder));
		}
	}

	public static final class Field extends IdentifiedTransform<FieldTransform> implements FieldTransform {
		public Field(Id owner, FieldTransform wrapped) {
			super(owner, wrapped);
		}

		@Override
		public void accept(FieldBuilder builder, FieldElement element) {
			this.withDetail(() -> this.wrapped.accept(builder, element));
		}

		@Override
		public void atStart(FieldBuilder builder) {
			this.withDetail(() -> this.wrapped.atStart(builder));
		}

		@Override
		public void atEnd(FieldBuilder builder) {
			this.withDetail(() -> this.wrapped.atEnd(builder));
		}
	}
}
