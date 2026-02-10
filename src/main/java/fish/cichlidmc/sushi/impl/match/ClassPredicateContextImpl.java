package fish.cichlidmc.sushi.impl.match;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.Objects;

public final class ClassPredicateContextImpl implements ClassPredicate.Context {
	private final ClassFileAccess classFile;

	@Nullable
	private final ClassDesc desc;
	private final byte[] bytes;

	@Nullable
	private ClassModel model;

	public ClassPredicateContextImpl(ClassFileAccess classFile, @Nullable ClassDesc desc, byte[] bytes) {
		this.classFile = classFile;
		this.desc = desc;
		this.bytes = bytes;
	}

	@Override
	public ClassDesc desc() {
		return Objects.requireNonNullElseGet(this.desc, () -> this.model().thisClass().asSymbol());
	}

	@Override
	public ClassModel model() {
		if (this.model == null) {
			this.model = this.classFile.get().parse(this.bytes);

			if (this.desc != null) {
				// make sure the actual desc matches the expected one
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

	@Override
	public ClassFileAccess classFile() {
		return this.classFile;
	}
}
