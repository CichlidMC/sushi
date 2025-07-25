package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.glavo.classfile.ClassModel;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link ClassModel} that is only loaded when actually needed.
 */
public sealed interface LazyClassModel {
	ClassDesc desc();

	ClassModel get();

	/**
	 * Create a LazyClassModel from an already loaded ClassModel.
	 */
	static LazyClassModel of(ClassModel model) {
		return new Resolved(model);
	}

	/**
	 * Create a new LazyClassModel from a possibly known name and a supplier.
	 * @param desc optional desc of the class if it's already known, otherwise the model will be read to query it.
	 * @param reader a supplier of the ClassModel. Will only be invoked once.
	 */
	static LazyClassModel of(@Nullable ClassDesc desc, Supplier<ClassModel> reader) {
		return new Impl(desc, reader);
	}

	final class Impl implements LazyClassModel {
		@Nullable
		private final ClassDesc desc;
		private final Supplier<ClassModel> reader;

		private ClassModel model;

		private Impl(@Nullable ClassDesc desc, Supplier<ClassModel> reader) {
			this.desc = desc;
			this.reader = reader;
		}

		@Override
		public ClassDesc desc() {
			return this.desc != null ? this.desc : this.get().thisClass().asSymbol();
		}

		@Override
		public ClassModel get() {
			if (this.model == null) {
				this.model = Objects.requireNonNull(this.reader.get());

				if (this.desc != null) {
					ClassDesc actualDesc = this.model.thisClass().asSymbol();
					if (!this.desc.equals(actualDesc)) {
						throw new IllegalArgumentException(String.format(
								"Name mismatch: '%s' vs '%s'",
								ClassDescs.fullName(this.desc), ClassDescs.fullName(actualDesc)
						));
					}
				}
			}

			return this.model;
		}
	}

	record Resolved(ClassModel model) implements LazyClassModel {
		@Override
		public ClassDesc desc() {
			return this.model.thisClass().asSymbol();
		}

		@Override
		public ClassModel get() {
			return this.model;
		}
	}
}
