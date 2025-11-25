package fish.cichlidmc.sushi.impl.phase;

import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.impl.transformer.PreparedTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record TransformPhase(int phase, List<PreparedTransform> transforms) {
	public static final Comparator<TransformPhase> BY_VALUE = Comparator.comparingInt(TransformPhase::phase);

	public TransformPhase(int phase, List<PreparedTransform> transforms) {
		if (transforms.isEmpty()) {
			throw new IllegalArgumentException("Phase must contain at least 1 transform");
		}

		this.phase = phase;
		this.transforms = Collections.unmodifiableList(transforms);
	}

	public TransformResult transform(ClassFile context, ClassModel model, boolean addMetadata, @Nullable ClassTransform andThen) {
		TransformContextImpl transformContext = new TransformContextImpl(model, addMetadata);
		ClassTransform transform = new PhaseTransform(this.transforms, transformContext);
		ClassTransform finalTransform = andThen == null ? transform : transform.andThen(andThen);
		byte[] bytes = context.transform(model, finalTransform);
		return new TransformResult(bytes, transformContext.collectRequirements());
	}
}
