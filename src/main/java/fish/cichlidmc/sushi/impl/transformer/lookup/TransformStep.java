package fish.cichlidmc.sushi.impl.transformer.lookup;

import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public record TransformStep(SequencedSet<PreparedTransform> transforms) {
	TransformStep() {
		this(new LinkedHashSet<>());
	}

	public TransformResult run(ClassFile context, ClassModel model, boolean metadata, @Nullable ClassTransform andThen) {
		TransformContextImpl transformContext = new TransformContextImpl(model, metadata);
		ClassTransform transform = new StepTransform(this.transforms, transformContext);
		ClassTransform finalTransform = andThen == null ? transform : transform.andThen(andThen);
		byte[] bytes = context.transformClass(model, finalTransform);
		return new TransformResult(bytes, transformContext.collectRequirements());
	}
}
