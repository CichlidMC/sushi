package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.impl.Transformation;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassTransform;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public record TransformStep(SequencedSet<PreparedTransform> transforms) {
	TransformStep() {
		this(new LinkedHashSet<>());
	}

	public byte[] run(Transformation transformation, @Nullable ClassTransform andThen) {
		ClassTransform transform = new SingleStepTransform(transformation.clazz(), this);
		ClassTransform finalTransform = andThen == null ? transform : transform.andThen(andThen);
		return transformation.context.transformClass(transformation.clazz().model(), finalTransform);
	}
}
