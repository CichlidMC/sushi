package fish.cichlidmc.sushi.impl.util;

import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.glavo.classfile.ClassModel;
import org.jspecify.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.function.Supplier;

public final class LazyClassModel {
	@Nullable
	private final ClassDesc desc;
	private final Supplier<ClassModel> reader;

	private ClassModel model;

	public LazyClassModel(@Nullable ClassDesc desc, Supplier<ClassModel> reader) {
		this.desc = desc;
		this.reader = reader;
	}

	public ClassDesc desc() {
		return this.desc != null ? this.desc : this.get().thisClass().asSymbol();
	}

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
